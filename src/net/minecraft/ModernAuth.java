package net.minecraft;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

final class ModernAuth {
    private static final String DEFAULT_CLIENT_ID = "00000000402b5328";
    private static final String DESKTOP_REDIRECT_URI = "https://login.live.com/oauth20_desktop.srf";
    private static final String LOOPBACK_HOST = "127.0.0.1";
    private static final String LOOPBACK_PATH = "/mclauncherrevival/oauth";
    private static final int LOOPBACK_TIMEOUT_SECONDS = 240;
    private static final int MAX_AUTH_RESPONSE_BYTES = 2 * 1024 * 1024;
    private static final String SCOPE = "XboxLive.signin offline_access";
    private static final String AUTHORIZE_URL = "https://login.live.com/oauth20_authorize.srf";
    private static final String TOKEN_URL = "https://login.live.com/oauth20_token.srf";
    private static final String XBL_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MC_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MC_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    private final TokenCache cache;
    private final StatusSink status;

    ModernAuth(TokenCache cache, StatusSink status) {
        this.cache = cache;
        this.status = status;
    }

    AuthProfile login() throws IOException {
        AuthProfile cached = cache.cachedProfile();
        if (cached != null) {
            status.status("Using cached Minecraft token for " + cached.name + ".");
            return cached;
        }
        if (cache.hasRefreshToken()) {
            try {
                status.status("Refreshing Microsoft session...");
                MicrosoftToken token = refreshMicrosoft(cache.get("ms.refresh_token"));
                return finishMinecraftLogin(token);
            } catch (IOException e) {
                status.status("Cached Microsoft token could not be refreshed; starting browser login.");
            }
        }
        MicrosoftToken token = interactiveLogin();
        return finishMinecraftLogin(token);
    }

    private MicrosoftToken interactiveLogin() throws IOException {
        int choice = status.choose("Microsoft Sign-In", trustMessage(), new String[] {
                "Open Microsoft Sign-In", "Cancel"
        });
        if (choice != 0) {
            throw new IOException("Microsoft login was cancelled.");
        }
        if (!loopbackEnabled()) {
            return desktopRedirectLogin(false);
        }
        try {
            return loopbackBrowserLogin();
        } catch (IOException primaryFailure) {
            status.status("Normal browser sign-in did not complete. Offering fallback options.");
            return fallbackAfterBrowserFailure(primaryFailure);
        }
    }

    private MicrosoftToken fallbackAfterBrowserFailure(IOException primaryFailure) throws IOException {
        int choice = status.choose("Microsoft Login Fallback", "Normal browser sign-in did not finish.\n\n"
                + safeReason(primaryFailure) + "\n\n"
                + "Use the paste-back fallback if this computer cannot receive the local browser callback.\n\n"
                + "This launcher still never asks for your raw Microsoft password.", new String[] {
                "Advanced Paste-Back", "Cancel"
        });
        if (choice == 0) {
            return manualPasteBackLogin();
        }
        throw new IOException("Microsoft login was cancelled.");
    }

    private MicrosoftToken loopbackBrowserLogin() throws IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        final String[] callbackUrl = new String[1];
        final IOException[] callbackError = new IOException[1];
        HttpServer server = HttpServer.create(new InetSocketAddress(LOOPBACK_HOST, 0), 0);
        int port = server.getAddress().getPort();
        final String redirectUri = "http://" + LOOPBACK_HOST + ":" + port + LOOPBACK_PATH;
        final String state = randomBase64Url(24);
        final String verifier = randomBase64Url(32);
        String loginUrl = authorizationUrl(redirectUri, state, verifier);

        server.createContext(LOOPBACK_PATH, new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())
                        || !LOOPBACK_PATH.equals(exchange.getRequestURI().getPath())) {
                    exchange.sendResponseHeaders(404, -1L);
                    exchange.close();
                    return;
                }
                try {
                    callbackUrl[0] = redirectUri + "?" + nullToEmpty(exchange.getRequestURI().getRawQuery());
                    sendBrowserPage(exchange, "MCLauncherRevival sign-in received",
                            "You can close this browser tab and return to MCLauncherRevival.");
                } catch (IOException e) {
                    callbackError[0] = e;
                    throw e;
                } finally {
                    latch.countDown();
                }
            }
        });
        server.setExecutor(null);
        server.start();
        try {
            status.status("Opening Microsoft sign-in in your default browser...");
            openBrowser(loginUrl);
            status.status("Waiting for Microsoft sign-in to return to this computer...");
            boolean received;
            try {
                received = latch.await(LOOPBACK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Microsoft login was interrupted.");
            }
            if (!received) {
                throw new IOException("The local browser callback was not received before the sign-in timeout.");
            }
            if (callbackError[0] != null) {
                throw callbackError[0];
            }
            return readAuthorizationCallback(callbackUrl[0], redirectUri, state, verifier);
        } finally {
            server.stop(0);
        }
    }

    private MicrosoftToken readAuthorizationCallback(String returnedUrl, String redirectUri, String state, String verifier) throws IOException {
        OAuthCallback.requireExpectedRedirect(returnedUrl, redirectUri);
        String returnedState = queryValue(returnedUrl, "state");
        if (!state.equals(returnedState)) {
            throw new IOException("Microsoft login returned an unexpected state value. Please try again.");
        }
        String error = queryValue(returnedUrl, "error_description");
        if (error == null || error.length() == 0) {
            error = queryValue(returnedUrl, "error");
        }
        if (error != null && error.length() > 0) {
            throw new IOException("Microsoft login was not approved: " + safeProviderMessage(error));
        }
        String code = queryValue(returnedUrl, "code");
        if (code == null || code.length() == 0) {
            throw new IOException("Microsoft login did not return an authorization code.");
        }
        status.status("Microsoft browser sign-in approved. Finishing token exchange...");
        return exchangeAuthorizationCode(code, redirectUri, verifier, TOKEN_URL);
    }

    private MicrosoftToken manualPasteBackLogin() throws IOException {
        return desktopRedirectLogin(true);
    }

    private MicrosoftToken desktopRedirectLogin(boolean advanced) throws IOException {
        final String state = randomBase64Url(24);
        final String verifier = randomBase64Url(32);
        final String loginUrl = authorizationUrl(DESKTOP_REDIRECT_URI, state, verifier);
        String title = advanced ? "Advanced Paste-Back Login" : "Microsoft Browser Login";
        String intro = advanced
                ? "Use this only if normal browser sign-in could not return to the launcher automatically.\n\n"
                : "This compatibility login uses Microsoft's registered desktop redirect.\n\n";
        int choice = status.choose(title, intro
                + "After Microsoft sign-in, copy only the final redirect URL from the browser address bar and paste it into the launcher.\n\n"
                + "Example shape:\n"
                + DESKTOP_REDIRECT_URI + "?code=...&state=...\n\n"
                + "If Microsoft shows removed=true on that page, copy the full address bar URL anyway. That usually means Microsoft returned to its desktop redirect page, not that your password was sent to the launcher.\n\n"
                + "Do not paste this URL into Discord, GitHub issues, screenshots, or support chats. It may contain a short-lived sign-in code.\n\n"
                + "Your Microsoft password still belongs only on Microsoft-owned websites.", new String[] {
                "Open Browser", "Cancel"
        });
        if (choice != 0) {
            throw new IOException("Microsoft login was cancelled.");
        }
        openBrowser(loginUrl);
        while (true) {
            String redirected = status.ask(title,
                    "After Microsoft sign-in, copy the full final URL from the browser address bar.\n\n"
                            + "It should start with:\n"
                            + DESKTOP_REDIRECT_URI + "?code=\n\n"
                            + "If the page text says removed=true, still copy the full browser address bar URL.\n\n"
                            + "Paste it here, or use the Paste from Clipboard button.\n\n"
                            + "Do not share this URL publicly.");
            if (redirected == null || redirected.trim().length() == 0) {
                throw new IOException("Microsoft login was cancelled.");
            }
            String trimmed = redirected.trim();
            if (trimmed.indexOf(DESKTOP_REDIRECT_URI) >= 0) {
                return readAuthorizationCallback(trimmed, DESKTOP_REDIRECT_URI, state, verifier);
            }
            int retry = status.choose("Microsoft Redirect URL Needed",
                    "That does not look like the final Microsoft redirect URL.\n\n"
                            + "Copy the full browser address after Microsoft sign-in. It should start with:\n"
                            + DESKTOP_REDIRECT_URI + "?code=\n\n"
                            + "Try again?", new String[] { "Try Again", "Cancel" });
            if (retry != 0) {
                throw new IOException("Microsoft login was cancelled.");
            }
        }
    }

    private MicrosoftToken exchangeAuthorizationCode(String code, String redirectUri, String verifier, String tokenUrl) throws IOException {
        LinkedHashMap<String, String> form = new LinkedHashMap<String, String>();
        form.put("client_id", clientId());
        form.put("code", code);
        form.put("grant_type", "authorization_code");
        form.put("redirect_uri", redirectUri);
        form.put("scope", SCOPE);
        form.put("code_verifier", verifier);
        HttpResult result = postForm(tokenUrl, form);
        ensureSuccess(result, "Microsoft authorization-code exchange failed");
        return readMicrosoftToken(Json.object(Json.parse(result.body)), tokenUrl);
    }

    private MicrosoftToken refreshMicrosoft(String refreshToken) throws IOException {
        String tokenUrl = cache.get("ms.token_url");
        if (tokenUrl == null || tokenUrl.length() == 0) {
            tokenUrl = TOKEN_URL;
        }
        LinkedHashMap<String, String> form = new LinkedHashMap<String, String>();
        form.put("client_id", clientId());
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", refreshToken);
        form.put("scope", SCOPE);
        HttpResult result = postForm(tokenUrl, form);
        ensureSuccess(result, "Microsoft refresh failed");
        return readMicrosoftToken(Json.object(Json.parse(result.body)), tokenUrl);
    }

    private MicrosoftToken readMicrosoftToken(Map<String, Object> json, String tokenUrl) throws IOException {
        String accessToken = Json.string(json, "access_token");
        String refreshToken = Json.string(json, "refresh_token");
        long expiresIn = Json.number(json, "expires_in", 3600L);
        if (accessToken == null || accessToken.length() == 0) {
            throw new IOException("Microsoft did not return an access token.");
        }
        cache.put("ms.access_token", accessToken);
        cache.put("ms.token_url", tokenUrl);
        if (refreshToken != null && refreshToken.length() > 0) {
            cache.put("ms.refresh_token", refreshToken);
        }
        cache.putLong("ms.expires_at", System.currentTimeMillis() + expiresIn * 1000L);
        cache.save();
        return new MicrosoftToken(accessToken);
    }

    private AuthProfile finishMinecraftLogin(MicrosoftToken token) throws IOException {
        status.status("Authenticating with Xbox Live...");
        XboxToken xbox = authenticateXboxLive(token.accessToken);
        status.status("Requesting XSTS token...");
        XboxToken xsts = authorizeXsts(xbox.token);
        status.status("Signing in to Minecraft services...");
        MinecraftToken minecraft = loginMinecraft(xsts.userHash, xsts.token);
        status.status("Fetching Minecraft profile...");
        AuthProfile profile = fetchProfile(minecraft.accessToken);

        cache.put("mc.access_token", minecraft.accessToken);
        cache.putLong("mc.expires_at", System.currentTimeMillis() + minecraft.expiresIn * 1000L);
        cache.put("profile.name", profile.name);
        cache.put("profile.id", profile.uuid);
        cache.save();

        status.status("Signed in as " + profile.name + ". Your Microsoft password was never entered into the launcher.");
        return profile;
    }

    private XboxToken authenticateXboxLive(String microsoftAccessToken) throws IOException {
        IOException firstFailure = null;
        try {
            return authenticateXboxLiveWithTicket("d=" + microsoftAccessToken);
        } catch (IOException e) {
            firstFailure = e;
        }
        try {
            return authenticateXboxLiveWithTicket(microsoftAccessToken);
        } catch (IOException e) {
            if (firstFailure != null) {
                throw firstFailure;
            }
            throw e;
        }
    }

    private XboxToken authenticateXboxLiveWithTicket(String rpsTicket) throws IOException {
        String json = "{"
                + "\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":"
                + Json.quote(rpsTicket)
                + "},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}";
        HttpResult result = postJson(XBL_AUTH_URL, json, null);
        ensureSuccess(result, "Xbox Live authentication failed");
        return readXboxToken(result.body);
    }

    private XboxToken authorizeXsts(String xboxToken) throws IOException {
        String json = "{"
                + "\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[" + Json.quote(xboxToken) + "]},"
                + "\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}";
        HttpResult result = postJson(XSTS_AUTH_URL, json, null);
        if (result.code == 401) {
            Map<String, Object> error = Json.object(Json.parse(result.body));
            String xerr = Json.string(error, "XErr");
            String redirect = Json.string(error, "Redirect");
            throw new IOException(friendlyXstsError(xerr, redirect));
        }
        ensureSuccess(result, "XSTS authorization failed");
        return readXboxToken(result.body);
    }

    private MinecraftToken loginMinecraft(String userHash, String xstsToken) throws IOException {
        String identityToken = "XBL3.0 x=" + userHash + ";" + xstsToken;
        String json = "{\"identityToken\":" + Json.quote(identityToken) + "}";
        HttpResult result = postJson(MC_LOGIN_URL, json, null);
        if (result.code == 401 || result.code == 403) {
            throw new IOException("Minecraft services rejected this account after Xbox authorization. "
                    + "Make sure this is the Microsoft account that owns Minecraft: Java Edition or has the right Game Pass entitlement. "
                    + "If needed, open the official Minecraft Launcher once to finish profile setup, then click Forget Login and try again. "
                    + "Offline Play remains available.");
        }
        ensureSuccess(result, "Minecraft services login failed");
        Map<String, Object> object = Json.object(Json.parse(result.body));
        String accessToken = Json.string(object, "access_token");
        long expiresIn = Json.number(object, "expires_in", 86400L);
        if (accessToken == null || accessToken.length() == 0) {
            throw new IOException("Minecraft services did not return an access token.");
        }
        return new MinecraftToken(accessToken, expiresIn);
    }

    private AuthProfile fetchProfile(String minecraftAccessToken) throws IOException {
        HttpResult result = get(MC_PROFILE_URL, minecraftAccessToken);
        if (result.code == 404) {
            throw new IOException("Microsoft sign-in worked, but Minecraft services did not return a Java Edition profile for this account. "
                    + "Make sure this is the Microsoft account that owns Minecraft: Java Edition. If you recently bought the game or use Game Pass, "
                    + "open the official Minecraft Launcher once to finish profile setup, then try MCLauncherRevival again. Offline Play remains available.");
        }
        ensureSuccess(result, "Minecraft profile lookup failed");
        Map<String, Object> object = Json.object(Json.parse(result.body));
        String name = Json.string(object, "name");
        String id = Json.string(object, "id");
        if (name == null || id == null) {
            throw new IOException("Minecraft profile response did not include name and UUID.");
        }
        return AuthProfile.online(name, id, minecraftAccessToken);
    }

    private static String friendlyXstsError(String xerr, String redirect) {
        if ("2148916233".equals(xerr)) {
            return "Microsoft sign-in worked, but this account does not appear to have an Xbox profile yet. "
                    + "Open https://start.ui.xboxlive.com/, finish Xbox profile setup, then click Forget Login and try Microsoft Login again. "
                    + "Offline Play remains available.";
        }
        if ("2148916238".equals(xerr)) {
            return "Microsoft sign-in worked, but Xbox authorization was blocked by family or child-account settings. "
                    + "Ask the family organizer to review Xbox/Minecraft privacy and online safety settings. "
                    + "This launcher cannot override Microsoft family restrictions. Offline Play remains available.";
        }
        if ("2148916235".equals(xerr) || "2148916236".equals(xerr) || "2148916237".equals(xerr)) {
            return "Microsoft sign-in worked, but Xbox/Minecraft services rejected this account for a region, account, or service-availability reason. "
                    + "Check Xbox account settings and Minecraft support using the same Microsoft account, then click Forget Login and try again. "
                    + redirectText(redirect);
        }
        return "XSTS authorization failed"
                + (xerr == null || xerr.length() == 0 ? "." : " with XErr " + xerr + ".")
                + " This usually means Xbox account setup, family settings, region, or account entitlement needs attention. "
                + "Click Forget Login after fixing the account and try Microsoft Login again. "
                + redirectText(redirect);
    }

    private static String redirectText(String redirect) {
        if (redirect == null || redirect.length() == 0) {
            return "Offline Play remains available.";
        }
        return "Microsoft/Xbox suggested this page: " + safeDisplayUrl(redirect) + " Offline Play remains available.";
    }

    private XboxToken readXboxToken(String body) throws IOException {
        Map<String, Object> object = Json.object(Json.parse(body));
        String token = Json.string(object, "Token");
        String userHash = null;
        Map<String, Object> displayClaims = Json.object(object.get("DisplayClaims"));
        List<Object> xui = Json.array(displayClaims == null ? null : displayClaims.get("xui"));
        if (xui != null && xui.size() > 0) {
            Map<String, Object> first = Json.object(xui.get(0));
            userHash = Json.string(first, "uhs");
        }
        if (token == null || userHash == null) {
            throw new IOException("Xbox response was missing Token or user hash.");
        }
        return new XboxToken(token, userHash);
    }

    private static String authorizationUrl(String redirectUri, String state, String verifier) throws IOException {
        LinkedHashMap<String, String> query = new LinkedHashMap<String, String>();
        query.put("client_id", clientId());
        query.put("redirect_uri", redirectUri);
        query.put("response_type", "code");
        query.put("scope", SCOPE);
        query.put("state", state);
        query.put("code_challenge", pkceChallenge(verifier));
        query.put("code_challenge_method", "S256");
        query.put("prompt", "select_account");
        query.put("lw", "1");
        query.put("fl", "dob,easi2");
        query.put("xsup", "1");
        query.put("nopa", "2");
        return AUTHORIZE_URL + "?" + formEncode(query);
    }

    private static String trustMessage() {
        String callbackText = loopbackEnabled()
                ? "After sign-in, the browser may briefly return to a local address like:\n"
                + "http://127.0.0.1/...\n\n"
                + "That local address is this launcher listening on your own computer so it can finish sign-in.\n\n"
                : "After sign-in, Microsoft may show a desktop redirect page. The launcher will ask you to paste that final Microsoft redirect URL so it can finish sign-in.\n\n"
                + "That URL can contain a short-lived sign-in code. Do not share it in screenshots, Discord, GitHub issues, or support chats.\n\n";
        return "MCLauncherRevival will open your default browser for Microsoft sign-in.\n\n"
                + "Check the address bar before entering anything:\n"
                + "- login.microsoftonline.com\n"
                + "- login.live.com\n"
                + "- microsoft.com\n\n"
                + "Your Microsoft password is entered only in the browser on Microsoft's website.\n"
                + "This launcher never asks for your raw Microsoft password.\n\n"
                + callbackText
                + "MCLauncherRevival is unofficial and is not affiliated with Mojang, Microsoft, Xbox, or Minecraft.";
    }

    private static boolean loopbackEnabled() {
        return booleanProperty("mclauncher.oauth.loopback")
                && !DEFAULT_CLIENT_ID.equals(clientId());
    }

    private static String clientId() {
        String property = System.getProperty("mclauncher.msClientId");
        if (property != null && property.trim().length() > 0) {
            return property.trim();
        }
        String env = System.getenv("MCLR_MICROSOFT_CLIENT_ID");
        if (env != null && env.trim().length() > 0) {
            return env.trim();
        }
        return DEFAULT_CLIENT_ID;
    }

    private static boolean booleanProperty(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            return false;
        }
        return "true".equalsIgnoreCase(value)
                || "1".equals(value)
                || "yes".equalsIgnoreCase(value);
    }

    private static void sendBrowserPage(HttpExchange exchange, String title, String message) throws IOException {
        String html = "<html><head><title>MCLauncherRevival</title></head>"
                + "<body style='font-family:Verdana,Arial,sans-serif;background:#111;color:#eee;margin:32px'>"
                + "<h2>" + htmlEscape(title) + "</h2>"
                + "<p>" + htmlEscape(message) + "</p>"
                + "<p style='color:#aaa'>Your Microsoft password was not entered into MCLauncherRevival.</p>"
                + "</body></html>";
        byte[] bytes = html.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream out = exchange.getResponseBody();
        try {
            out.write(bytes);
        } finally {
            out.close();
        }
    }

    private static void openBrowser(String uri) {
        if (uri == null || uri.length() == 0) {
            return;
        }
        if (openWithJavaDesktop(uri)) {
            return;
        }
        if (windowsOs()) {
            openBrowserWindows(uri);
        } else if (macOs()) {
            openBrowserMac(uri);
        } else {
            openBrowserGeneric(uri);
        }
    }

    private static boolean openWithJavaDesktop(String uri) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(uri));
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean openBrowserWindows(String uri) {
        if (tryStart("rundll32", "url.dll,FileProtocolHandler", uri)) {
            return true;
        }
        return tryStart("cmd", "/c", "start", "", uri);
    }

    private static boolean openBrowserMac(String uri) {
        return tryStart("open", uri);
    }

    private static boolean openBrowserGeneric(String uri) {
        return tryStart("xdg-open", uri)
                || tryStart("gio", "open", uri)
                || tryStart("sensible-browser", uri);
    }

    private static boolean windowsOs() {
        return osName().indexOf("win") >= 0;
    }

    private static boolean macOs() {
        return osName().indexOf("mac") >= 0;
    }

    private static String osName() {
        return System.getProperty("os.name", "").toLowerCase();
    }

    private static String safeDisplayUrl(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            URI uri = new URI(value);
            StringBuilder safe = new StringBuilder();
            if (uri.getScheme() != null) {
                safe.append(uri.getScheme()).append("://");
            }
            if (uri.getHost() != null) {
                safe.append(uri.getHost());
            }
            if (uri.getPath() != null) {
                safe.append(uri.getPath());
            }
            return safe.length() == 0 ? "(link omitted)" : safe.toString();
        } catch (Exception ignored) {
            int query = value.indexOf('?');
            int fragment = value.indexOf('#');
            int end = value.length();
            if (query >= 0 && query < end) {
                end = query;
            }
            if (fragment >= 0 && fragment < end) {
                end = fragment;
            }
            return end > 0 ? value.substring(0, end) : "(link omitted)";
        }
    }

    private static boolean tryStart(String... command) {
        try {
            new ProcessBuilder(command).start();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static HttpResult postForm(String endpoint, Map<String, String> fields) throws IOException {
        byte[] bytes = formEncode(fields).getBytes("UTF-8");
        return request("POST", endpoint, "application/x-www-form-urlencoded", null, bytes);
    }

    private static String formEncode(Map<String, String> fields) throws IOException {
        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (body.length() > 0) {
                body.append('&');
            }
            body.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            body.append('=');
            body.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return body.toString();
    }

    private static String queryValue(String url, String key) throws IOException {
        int question = url.indexOf('?');
        int hash = url.indexOf('#');
        String[] sections = new String[2];
        if (question >= 0) {
            int end = hash > question ? hash : url.length();
            sections[0] = url.substring(question + 1, end);
        }
        if (hash >= 0) {
            sections[1] = url.substring(hash + 1);
        }
        for (int s = 0; s < sections.length; s++) {
            if (sections[s] == null) {
                continue;
            }
            String[] parts = sections[s].split("&");
            for (int i = 0; i < parts.length; i++) {
                int equals = parts[i].indexOf('=');
                String name = equals >= 0 ? parts[i].substring(0, equals) : parts[i];
                String value = equals >= 0 ? parts[i].substring(equals + 1) : "";
                if (key.equals(URLDecoder.decode(name, "UTF-8"))) {
                    return URLDecoder.decode(value, "UTF-8");
                }
            }
        }
        return null;
    }

    private static String pkceChallenge(String verifier) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return base64UrlNoPadding(digest.digest(verifier.getBytes("US-ASCII")));
        } catch (Exception e) {
            throw new IOException("Could not create PKCE challenge.");
        }
    }

    private static String randomBase64Url(int bytes) {
        byte[] data = new byte[bytes];
        RANDOM.nextBytes(data);
        return base64UrlNoPadding(data);
    }

    private static String base64UrlNoPadding(byte[] bytes) {
        StringBuilder out = new StringBuilder((bytes.length * 4 + 2) / 3);
        int i = 0;
        while (i < bytes.length) {
            int b0 = bytes[i++] & 0xff;
            int b1 = i < bytes.length ? bytes[i++] & 0xff : -1;
            int b2 = i < bytes.length ? bytes[i++] & 0xff : -1;
            out.append(BASE64[(b0 >>> 2) & 0x3f]);
            if (b1 >= 0) {
                out.append(BASE64[((b0 << 4) | (b1 >>> 4)) & 0x3f]);
                if (b2 >= 0) {
                    out.append(BASE64[((b1 << 2) | (b2 >>> 6)) & 0x3f]);
                    out.append(BASE64[b2 & 0x3f]);
                } else {
                    out.append(BASE64[(b1 << 2) & 0x3f]);
                }
            } else {
                out.append(BASE64[(b0 << 4) & 0x3f]);
            }
        }
        String value = out.toString();
        value = value.replace('+', '-').replace('/', '_');
        return value;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String safeReason(Throwable t) {
        if (t == null || t.getMessage() == null || t.getMessage().length() == 0) {
            return "No detailed reason was reported.";
        }
        return t.getMessage();
    }

    private static String htmlEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static HttpResult postJson(String endpoint, String body, String bearerToken) throws IOException {
        return request("POST", endpoint, "application/json", bearerToken, body.getBytes("UTF-8"));
    }

    private static HttpResult get(String endpoint, String bearerToken) throws IOException {
        return request("GET", endpoint, null, bearerToken, null);
    }

    private static HttpResult request(
            String method,
            String endpoint,
            String contentType,
            String bearerToken,
            byte[] body) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) DownloadClient.validatedUrl(endpoint).openConnection();
        try {
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "MCLauncherRevival/0.7");
            if (contentType != null) {
                connection.setRequestProperty("Content-Type", contentType);
            }
            if (bearerToken != null) {
                connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            }
            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Length", Integer.toString(body.length));
                OutputStream out = connection.getOutputStream();
                try {
                    out.write(body);
                } finally {
                    out.close();
                }
            }
            int code = connection.getResponseCode();
            InputStream in = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String response = readAll(in == null ? new ByteArrayInputStream(new byte[0]) : in);
            return new HttpResult(code, response);
        } finally {
            connection.disconnect();
        }
    }

    private static String readAll(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder out = new StringBuilder();
        char[] buffer = new char[4096];
        int read;
        try {
            while ((read = reader.read(buffer)) != -1) {
                if (out.length() + read > MAX_AUTH_RESPONSE_BYTES) {
                    throw new IOException("Authentication service response exceeded the allowed size.");
                }
                out.append(buffer, 0, read);
            }
        } finally {
            reader.close();
        }
        return out.toString();
    }

    private static void ensureSuccess(HttpResult result, String label) throws IOException {
        if (result.code < 200 || result.code >= 300) {
            String providerCode = safeProviderErrorCode(result.body);
            throw new IOException(label + " (HTTP " + result.code + ")"
                    + (providerCode.length() == 0 ? "." : " [" + providerCode + "]."));
        }
    }

    private static String safeProviderErrorCode(String body) {
        if (body == null || body.length() == 0) {
            return "";
        }
        try {
            Map<String, Object> object = Json.object(Json.parse(body));
            String value = Json.string(object, "error");
            if (value == null || value.length() == 0) {
                value = Json.string(object, "XErr");
            }
            if (value == null || value.length() == 0 || value.length() > 80) {
                return "";
            }
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (!Character.isLetterOrDigit(c) && c != '_' && c != '-' && c != '.' && c != ':') {
                    return "";
                }
            }
            return value;
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String safeProviderMessage(String value) {
        if (value == null || value.length() == 0) {
            return "No reason was provided.";
        }
        String clean = value.replace('\n', ' ').replace('\r', ' ');
        clean = clean.replaceAll("(?i)(access_token|refresh_token|code)=([^&\\s]+)", "$1=[redacted]");
        return clean.length() > 300 ? clean.substring(0, 300) + "..." : clean;
    }

    private static final class MicrosoftToken {
        final String accessToken;

        MicrosoftToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }

    private static final class XboxToken {
        final String token;
        final String userHash;

        XboxToken(String token, String userHash) {
            this.token = token;
            this.userHash = userHash;
        }
    }

    private static final class MinecraftToken {
        final String accessToken;
        final long expiresIn;

        MinecraftToken(String accessToken, long expiresIn) {
            this.accessToken = accessToken;
            this.expiresIn = expiresIn;
        }
    }

    private static final class HttpResult {
        final int code;
        final String body;

        HttpResult(int code, String body) {
            this.code = code;
            this.body = body == null ? "" : body;
        }
    }
}
