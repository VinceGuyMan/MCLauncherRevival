package net.minecraft;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ModernAuth {
    private static final String CLIENT_ID = "00000000402b5328";
    private static final String REDIRECT_URI = "https://login.live.com/oauth20_desktop.srf";
    private static final String SCOPE = "service::user.auth.xboxlive.com::MBI_SSL";
    private static final String AUTHORIZE_URL = "https://login.live.com/oauth20_authorize.srf";
    private static final String TOKEN_URL = "https://login.live.com/oauth20_token.srf";
    private static final String XBL_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MC_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MC_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

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
        MicrosoftToken token = browserLogin();
        return finishMinecraftLogin(token);
    }

    private MicrosoftToken browserLogin() throws IOException {
        try {
            return windowsDesktopBrowserLogin();
        } catch (IOException e) {
            status.status("Windows browser helper did not capture login; falling back to pasted URL.");
        }

        String loginUrl = AUTHORIZE_URL
                + "?client_id=" + encode(CLIENT_ID)
                + "&redirect_uri=" + encode(REDIRECT_URI)
                + "&response_type=token"
                + "&scope=" + encode(SCOPE)
                + "&prompt=select_account"
                + "&lw=1&fl=dob,easi2&xsup=1&nopa=2";

        status.status("Opening Microsoft browser login...");
        openBrowser(loginUrl);
        String redirected = status.ask("Microsoft Login",
                "Sign in in the browser window that opened.\n\n"
                        + "When it redirects to a mostly blank page, copy the full address from the browser bar and paste it here.\n"
                        + "If it changes to removed=true, try again and copy it as soon as the blank page appears.\n\n"
                        + "It should start with:\n"
                        + REDIRECT_URI + "#access_token=");
        if (redirected == null || redirected.trim().length() == 0) {
            throw new IOException("Microsoft login was cancelled.");
        }
        String accessToken = queryValue(redirected.trim(), "access_token");
        if (accessToken != null && accessToken.length() > 0) {
            return new MicrosoftToken(accessToken);
        }
        String code = queryValue(redirected.trim(), "code");
        if (code == null || code.length() == 0) {
            String error = queryValue(redirected.trim(), "error_description");
            if (error == null) {
                error = queryValue(redirected.trim(), "error");
            }
            throw new IOException("Could not find a Microsoft authorization code in the pasted URL"
                    + (error == null ? "." : ": " + error));
        }

        return exchangeAuthorizationCode(code, REDIRECT_URI);
    }

    private MicrosoftToken windowsDesktopBrowserLogin() throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.indexOf("win") < 0) {
            throw new IOException("Windows browser helper is only available on Windows.");
        }

        File outFile = File.createTempFile("mclauncher-oauth-", ".txt");
        File scriptFile = File.createTempFile("mclauncher-oauth-", ".ps1");
        outFile.delete();

            String loginUrl = AUTHORIZE_URL
                    + "?client_id=" + encode(CLIENT_ID)
                    + "&redirect_uri=" + encode(REDIRECT_URI)
                    + "&response_type=token"
                    + "&scope=" + encode(SCOPE)
                    + "&prompt=select_account"
                    + "&lw=1&fl=dob,easi2&xsup=1&nopa=2";

        String script = "$ErrorActionPreference = 'Stop'\r\n"
                + "$url = " + psQuote(loginUrl) + "\r\n"
                + "$out = " + psQuote(outFile.getAbsolutePath()) + "\r\n"
                + "$ie = New-Object -ComObject InternetExplorer.Application\r\n"
                + "$ie.Visible = $true\r\n"
                + "$ie.Navigate2($url)\r\n"
                + "$deadline = (Get-Date).AddMinutes(5)\r\n"
                + "while ((Get-Date) -lt $deadline) {\r\n"
                + "  Start-Sleep -Milliseconds 100\r\n"
                + "  try { $loc = $ie.LocationURL } catch { $loc = '' }\r\n"
                + "  if ($loc -like 'https://login.live.com/oauth20_desktop.srf*' -and ($loc -match 'code=' -or $loc -match 'access_token=')) {\r\n"
                + "    [IO.File]::WriteAllText($out, $loc, [Text.Encoding]::UTF8)\r\n"
                + "    Start-Sleep -Milliseconds 300\r\n"
                + "    try { $ie.Quit() } catch {}\r\n"
                + "    exit 0\r\n"
                + "  }\r\n"
                + "}\r\n"
                + "try { $ie.Quit() } catch {}\r\n"
                + "exit 2\r\n";
        writeText(scriptFile, script);

        status.status("Opening Microsoft login in a Windows browser helper...");
        Process process;
        try {
            process = new ProcessBuilder("powershell", "-NoProfile", "-ExecutionPolicy", "Bypass",
                    "-File", scriptFile.getAbsolutePath()).start();
        } catch (IOException e) {
            throw new IOException("Could not start PowerShell browser helper.", e);
        }

        int exit;
        try {
            exit = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Microsoft login was interrupted.");
        } finally {
            scriptFile.delete();
        }

        if (exit != 0 || !outFile.exists()) {
            outFile.delete();
            throw new IOException("Browser helper did not capture the OAuth redirect.");
        }

        String redirected = readAll(new java.io.FileInputStream(outFile)).trim();
        outFile.delete();
        String accessToken = queryValue(redirected, "access_token");
        if (accessToken != null && accessToken.length() > 0) {
            return new MicrosoftToken(accessToken);
        }
        String code = queryValue(redirected, "code");
        if (code == null || code.length() == 0) {
            throw new IOException("Browser helper captured the redirect, but no code was present.");
        }
        return exchangeAuthorizationCode(code, REDIRECT_URI);
    }

    private MicrosoftToken exchangeAuthorizationCode(String code, String redirectUri) throws IOException {
        LinkedHashMap<String, String> form = new LinkedHashMap<String, String>();
        form.put("client_id", CLIENT_ID);
        form.put("code", code);
        form.put("grant_type", "authorization_code");
        form.put("redirect_uri", redirectUri);
        form.put("scope", SCOPE);
        HttpResult result = postForm(TOKEN_URL, form);
        ensureSuccess(result, "Microsoft authorization-code exchange failed");
        return readMicrosoftToken(Json.object(Json.parse(result.body)));
    }

    private MicrosoftToken refreshMicrosoft(String refreshToken) throws IOException {
        LinkedHashMap<String, String> form = new LinkedHashMap<String, String>();
        form.put("client_id", CLIENT_ID);
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", refreshToken);
        form.put("scope", SCOPE);
        HttpResult result = postForm(TOKEN_URL, form);
        ensureSuccess(result, "Microsoft refresh failed");
        return readMicrosoftToken(Json.object(Json.parse(result.body)));
    }

    private MicrosoftToken readMicrosoftToken(Map<String, Object> json) throws IOException {
        String accessToken = Json.string(json, "access_token");
        String refreshToken = Json.string(json, "refresh_token");
        long expiresIn = Json.number(json, "expires_in", 3600L);
        if (accessToken == null || accessToken.length() == 0) {
            throw new IOException("Microsoft did not return an access token.");
        }
        cache.put("ms.access_token", accessToken);
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

        status.status("Logged in as " + profile.name + ".");
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
            if ("2148916233".equals(xerr)) {
                throw new IOException("This Microsoft account has no Xbox profile yet.");
            }
            if ("2148916238".equals(xerr)) {
                throw new IOException("This account is a child account and needs family approval for Xbox Live.");
            }
        }
        ensureSuccess(result, "XSTS authorization failed");
        return readXboxToken(result.body);
    }

    private MinecraftToken loginMinecraft(String userHash, String xstsToken) throws IOException {
        String identityToken = "XBL3.0 x=" + userHash + ";" + xstsToken;
        String json = "{\"identityToken\":" + Json.quote(identityToken) + "}";
        HttpResult result = postJson(MC_LOGIN_URL, json, null);
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
            throw new IOException("This Microsoft account does not appear to own Minecraft Java Edition.");
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

    private static void openBrowser(String uri) {
        if (uri == null || uri.length() == 0) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(uri));
            }
        } catch (Throwable ignored) {
        }
    }

    private static HttpResult postForm(String endpoint, Map<String, String> fields) throws IOException {
        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (body.length() > 0) {
                body.append('&');
            }
            body.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            body.append('=');
            body.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        byte[] bytes = body.toString().getBytes("UTF-8");
        return request("POST", endpoint, "application/x-www-form-urlencoded", null, bytes);
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

    private static String encode(String value) throws IOException {
        return URLEncoder.encode(value, "UTF-8");
    }

    private static String psQuote(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    private static void writeText(File file, String value) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(value.getBytes("UTF-8"));
        } finally {
            out.close();
        }
    }

    private static HttpResult postJson(String endpoint, String body, String bearerToken) throws IOException {
        return request("POST", endpoint, "application/json", bearerToken, body.getBytes("UTF-8"));
    }

    private static HttpResult get(String endpoint, String bearerToken) throws IOException {
        return request("GET", endpoint, null, bearerToken, null);
    }

    private static HttpResult request(String method, String endpoint, String contentType, String bearerToken, byte[] body) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        connection.setRequestMethod(method);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "MCLauncherRevive/1.0 Java8");
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
