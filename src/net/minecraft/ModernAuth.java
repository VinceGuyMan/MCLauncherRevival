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
import java.net.URL;
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
    private static final String CLIENT_ID = "00000000402b5328";
    private static final String DESKTOP_REDIRECT_URI = "https://login.live.com/oauth20_desktop.srf";
    private static final String LOOPBACK_HOST = "127.0.0.1";
    private static final String LOOPBACK_PATH = "/mclauncherrevival/oauth";
    private static final int LOOPBACK_TIMEOUT_SECONDS = 240;
    private static final String SCOPE = "XboxLive.signin offline_access";
    private static final String AUTHORIZE_URL = "https://login.live.com/oauth20_authorize.srf";
    private static final String TOKEN_URL = "https://login.live.com/oauth20_token.srf";
    private static final String DEVICE_CODE_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
    private static final String AAD_TOKEN_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
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
                "Open Microsoft Sign-In", "Use Code Login Instead", "Cancel"
        });
        if (choice == 2 || choice < 0) {
            throw new IOException("Microsoft login was cancelled.");
        }
        if (choice == 1) {
            return deviceCodeOrAdvanced(null);
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
                + "You can try code login, or use the advanced paste-back fallback if this computer cannot receive the local browser callback.\n\n"
                + "This launcher still never asks for your raw Microsoft password.", new String[] {
                "Use Code Login", "Advanced Paste-Back", "Cancel"
        });
        if (choice == 0) {
            return deviceCodeOrAdvanced(primaryFailure);
        }
        if (choice == 1) {
            return manualPasteBackLogin();
        }
        throw new IOException("Microsoft login was cancelled.");
    }

    private MicrosoftToken deviceCodeOrAdvanced(IOException previousFailure) throws IOException {
        try {
            return deviceCodeLogin();
        } catch (IOException deviceFailure) {
            StringBuilder message = new StringBuilder();
            message.append("Code login did not finish.\n\n");
            if (previousFailure != null) {
                message.append("Normal browser sign-in: ").append(safeReason(previousFailure)).append("\n\n");
            }
            message.append("Code login: ").append(safeReason(deviceFailure)).append("\n\n");
            message.append("Use advanced paste-back only if the normal browser callback and code login both failed.");
            int choice = status.choose("Advanced Login Fallback", message.toString(), new String[] {
                    "Advanced Paste-Back", "Cancel"
            });
            if (choice == 0) {
                return manualPasteBackLogin();
            }
            throw deviceFailure;
        }
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
        String returnedState = queryValue(returnedUrl, "state");
        if (!state.equals(returnedState)) {
            throw new IOException("Microsoft login returned an unexpected state value. Please try again.");
        }
        String error = queryValue(returnedUrl, "error_description");
        if (error == null || error.length() == 0) {
            error = queryValue(returnedUrl, "error");
        }
        if (error != null && error.length() > 0) {
            throw new IOException("Microsoft login was not approved: " + error);
        }
        String code = queryValue(returnedUrl, "code");
        if (code == null || code.length() == 0) {
            throw new IOException("Microsoft login did not return an authorization code.");
        }
        status.status("Microsoft browser sign-in approved. Finishing token exchange...");
        return exchangeAuthorizationCode(code, redirectUri, verifier, TOKEN_URL);
    }

    private MicrosoftToken manualPasteBackLogin() throws IOException {
        final String state = randomBase64Url(24);
        final String verifier = randomBase64Url(32);
        final String loginUrl = authorizationUrl(DESKTOP_REDIRECT_URI, state, verifier);
        int choice = status.choose("Advanced Paste-Back Login", "Use this only if normal browser sign-in and code login failed.\n\n"
                + "After Microsoft sign-in, copy only the final redirect URL from the browser address bar and paste it into the launcher.\n\n"
                + "Do not paste this URL into Discord, GitHub issues, screenshots, or support chats. It may contain a short-lived sign-in code.\n\n"
                + "Your Microsoft password still belongs only on Microsoft-owned websites.", new String[] {
                "Open Browser", "Cancel"
        });
        if (choice != 0) {
            throw new IOException("Microsoft login was cancelled.");
        }
        openBrowser(loginUrl);
        String redirected = status.ask("Advanced Paste-Back Login",
                "Paste the final Microsoft redirect URL here.\n\n"
                        + "It should start with:\n"
                        + DESKTOP_REDIRECT_URI + "?code=\n\n"
                        + "Do not share this URL publicly.");
        if (redirected == null || redirected.trim().length() == 0) {
            throw new IOException("Microsoft login was cancelled.");
        }
        String trimmed = redirected.trim();
        if (trimmed.indexOf(DESKTOP_REDIRECT_URI) < 0) {
            throw new IOException("That does not look like the expected Microsoft desktop redirect URL.");
        }
        return readAuthorizationCallback(trimmed, DESKTOP_REDIRECT_URI, state, verifier);
    }

    private MicrosoftToken deviceCodeLogin() throws IOException {
        LinkedHashMap<String, String> form = new LinkedHashMap<String, String>();
        form.put("client_id", CLIENT_ID);
        form.put("scope", SCOPE);
        HttpResult setup = postForm(DEVICE_CODE_URL, form);
        ensureSuccess(setup, "Microsoft device-code request failed");
        Map<String, Object> json = Json.object(Json.parse(setup.body));
        final String deviceCode = Json.string(json, "device_code");
        String userCode = Json.string(json, "user_code");
        String verificationUri = Json.string(json, "verification_uri");
        if (verificationUri == null || verificationUri.length() == 0) {
            verificationUri = Json.string(json, "verification_url");
        }
        long expiresIn = Json.number(json, "expires_in", 900L);
        int interval = (int) Json.number(json, "interval", 5L);
        String message = Json.string(json, "message");
        if (deviceCode == null || userCode == null || verificationUri == null) {
            throw new IOException("Microsoft device-code response was missing required fields.");
        }

        int choice = status.choose("Microsoft Code Login", "Use this only if normal browser sign-in did not work.\n\n"
                + "1. Open the Microsoft page shown below.\n"
                + "2. Enter this code: " + userCode + "\n"
                + "3. Confirm the page says you are signing in to MCLauncherRevival or a Microsoft/Xbox sign-in app you trust.\n"
                + "4. If it shows a different or suspicious app name, cancel.\n\n"
                + "Page:\n" + verificationUri + "\n\n"
                + (message == null ? "" : message + "\n\n")
                + "This launcher never needs your Microsoft password. Only enter your password on Microsoft's website.", new String[] {
                "Open Microsoft Code Page", "Cancel"
        });
        if (choice != 0) {
            throw new IOException("Microsoft code login was cancelled.");
        }
        openBrowser(verificationUri);
        status.status("Waiting for Microsoft code login approval...");
        return pollDeviceCode(deviceCode, expiresIn, interval);
    }

    private MicrosoftToken pollDeviceCode(String deviceCode, long expiresIn, int interval) throws IOException {
        long deadline = System.currentTimeMillis() + expiresIn * 1000L;
        int sleepSeconds = Math.max(3, interval);
        while (System.currentTimeMillis() < deadline) {
            LinkedHashMap<String, String> form = new LinkedHashMap<String, String>();
            form.put("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
            form.put("client_id", CLIENT_ID);
            form.put("device_code", deviceCode);
            HttpResult result = postForm(AAD_TOKEN_URL, form);
            if (result.code >= 200 && result.code < 300) {
                status.status("Microsoft code login approved. Finishing sign-in...");
                return readMicrosoftToken(Json.object(Json.parse(result.body)), AAD_TOKEN_URL);
            }
            Map<String, Object> error = parseObjectQuietly(result.body);
            String errorCode = Json.string(error, "error");
            String description = Json.string(error, "error_description");
            if ("authorization_pending".equals(errorCode)) {
                sleep(sleepSeconds);
                continue;
            }
            if ("slow_down".equals(errorCode)) {
                sleepSeconds += 5;
                sleep(sleepSeconds);
                continue;
            }
            if ("expired_token".equals(errorCode)) {
                throw new IOException("Microsoft code login expired before approval. Try Microsoft Login again.");
            }
            if ("authorization_declined".equals(errorCode)) {
                throw new IOException("Microsoft code login was declined in the browser.");
            }
            throw new IOException("Microsoft code login failed"
                    + (description == null || description.length() == 0 ? "." : ": " + description));
        }
        throw new IOException("Microsoft code login expired before approval. Try Microsoft Login again.");
    }

    private MicrosoftToken exchangeAuthorizationCode(String code, String redirectUri, String verifier, String tokenUrl) throws IOException {
        LinkedHashMap<String, String> form = new LinkedHashMap<String, String>();
        form.put("client_id", CLIENT_ID);
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
        form.put("client_id", CLIENT_ID);
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
        return "Microsoft/Xbox suggested this page: " + redirect + " Offline Play remains available.";
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
        query.put("client_id", CLIENT_ID);
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
        return "MCLauncherRevival will open your default browser for Microsoft sign-in.\n\n"
                + "Check the address bar before entering anything:\n"
                + "- login.microsoftonline.com\n"
                + "- login.live.com\n"
                + "- microsoft.com\n\n"
                + "Your Microsoft password is entered only in the browser on Microsoft's website.\n"
                + "This launcher never asks for your raw Microsoft password.\n\n"
                + "After sign-in, the browser may briefly return to a local address like:\n"
                + "http://127.0.0.1/...\n\n"
                + "That local address is this launcher listening on your own computer so it can finish sign-in.\n\n"
                + "MCLauncherRevival is unofficial and is not affiliated with Mojang, Microsoft, Xbox, or Minecraft.";
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
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(uri));
                return;
            }
        } catch (Throwable ignored) {
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.indexOf("mac") >= 0) {
            if (tryStart("open", uri)) {
                return;
            }
        }
        if (os.indexOf("linux") >= 0 || os.indexOf("nux") >= 0
                || os.indexOf("nix") >= 0 || os.indexOf("aix") >= 0) {
            if (tryStart("xdg-open", uri)) {
                return;
            }
            if (tryStart("gio", "open", uri)) {
                return;
            }
            if (tryStart("sensible-browser", uri)) {
                return;
            }
        }
        if (os.indexOf("win") >= 0) {
            if (tryStart("rundll32", "url.dll,FileProtocolHandler", uri)) {
                return;
            }
            tryStart("cmd", "/c", "start", "", uri);
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

    private static Map<String, Object> parseObjectQuietly(String body) {
        try {
            return Json.object(Json.parse(body));
        } catch (Throwable ignored) {
            return null;
        }
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

    private static void sleep(int seconds) throws IOException {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Microsoft login was interrupted.");
        }
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
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        connection.setRequestMethod(method);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "MCLauncherRevival/alpha");
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
    }

    private static String readAll(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder out = new StringBuilder();
        char[] buffer = new char[4096];
        int read;
        while ((read = reader.read(buffer)) != -1) {
            out.append(buffer, 0, read);
        }
        return out.toString();
    }

    private static void ensureSuccess(HttpResult result, String label) throws IOException {
        if (result.code < 200 || result.code >= 300) {
            throw new IOException(label + " (HTTP " + result.code + "): " + result.body);
        }
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
