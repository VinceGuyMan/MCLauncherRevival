package net.minecraft;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Locale;

final class DownloadClient {
    private static final int CONNECT_TIMEOUT_MILLIS = 30000;
    private static final int READ_TIMEOUT_MILLIS = 30000;
    private static final int MAX_REDIRECTS = 5;
    private static final int MAX_TEXT_BYTES = 16 * 1024 * 1024;
    private static final long MAX_FILE_BYTES = 512L * 1024L * 1024L;
    private static final int MAX_IMAGE_BYTES = 16 * 1024 * 1024;

    private DownloadClient() {
    }

    static String getString(String url) throws IOException {
        return new String(getBytes(url, MAX_TEXT_BYTES), "UTF-8");
    }

    static byte[] getImageBytes(String url) throws IOException {
        return getBytes(url, MAX_IMAGE_BYTES);
    }

    static void downloadFile(String url, File file, String expectedSha1) throws IOException {
        if (file.exists() && (empty(expectedSha1) || sha1(file).equalsIgnoreCase(expectedSha1))) {
            return;
        }
        File parent = file.getParentFile();
        SafeFiles.ensureDirectory(parent);
        File temp = File.createTempFile("mclr-", ".download", parent);
        boolean moved = false;
        HttpURLConnection connection = null;
        try {
            connection = open(url);
            long declared = connection.getContentLength();
            if (declared > MAX_FILE_BYTES) {
                throw new IOException("Download is larger than the allowed limit: " + safeEndpoint(connection.getURL()));
            }
            InputStream in = connection.getInputStream();
            FileOutputStream out = new FileOutputStream(temp);
            try {
                copyBounded(in, out, MAX_FILE_BYTES);
                out.getFD().sync();
            } finally {
                try {
                    out.close();
                } finally {
                    in.close();
                }
            }
            if (!empty(expectedSha1) && !sha1(temp).equalsIgnoreCase(expectedSha1)) {
                throw new IOException("SHA-1 check failed for " + file.getName());
            }
            SafeFiles.atomicReplace(temp, file);
            moved = true;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (!moved) {
                temp.delete();
            }
        }
    }

    static URL validatedUrl(String raw) throws IOException {
        if (raw == null || raw.trim().length() == 0) {
            throw new IOException("Missing download URL.");
        }
        URL url;
        try {
            url = new URL(raw.trim());
        } catch (Exception e) {
            throw new IOException("Invalid download URL.");
        }
        String protocol = url.getProtocol() == null ? "" : url.getProtocol().toLowerCase(Locale.ENGLISH);
        if ("http".equals(protocol) && canUpgradeOfficialHost(url)) {
            if (url.getPort() != -1 && url.getPort() != 80) {
                throw new IOException("Refusing nonstandard insecure download port.");
            }
            url = new URL("https", url.getHost(), -1, url.getFile());
            protocol = "https";
        }
        if (!"https".equals(protocol)) {
            throw new IOException("Refusing non-HTTPS download URL: " + safeEndpoint(url));
        }
        if (url.getHost() == null || url.getHost().length() == 0 || url.getUserInfo() != null) {
            throw new IOException("Invalid HTTPS download URL.");
        }
        if (url.getPort() != -1 && url.getPort() != 443) {
            throw new IOException("Refusing nonstandard HTTPS download port.");
        }
        return url;
    }

    private static byte[] getBytes(String url, int maxBytes) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = open(url);
            int declared = connection.getContentLength();
            if (declared > maxBytes) {
                throw new IOException("Response is larger than the allowed limit: " + safeEndpoint(connection.getURL()));
            }
            InputStream in = connection.getInputStream();
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream(declared > 0 ? declared : 8192);
                copyBounded(in, out, maxBytes);
                return out.toByteArray();
            } finally {
                in.close();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static HttpURLConnection open(String raw) throws IOException {
        URL current = validatedUrl(raw);
        for (int redirects = 0; redirects <= MAX_REDIRECTS; redirects++) {
            HttpURLConnection connection = (HttpURLConnection) current.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(READ_TIMEOUT_MILLIS);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "MCLauncherRevival/0.7");
            connection.setRequestProperty("Accept", "*/*");
            int code = connection.getResponseCode();
            if (isRedirect(code)) {
                String location = connection.getHeaderField("Location");
                closeQuietly(connection.getErrorStream());
                closeQuietly(safeInputStream(connection));
                connection.disconnect();
                if (location == null || location.trim().length() == 0) {
                    throw new IOException("Download redirect did not include a destination.");
                }
                if (redirects == MAX_REDIRECTS) {
                    throw new IOException("Download exceeded the redirect limit.");
                }
                current = validatedUrl(new URL(current, location).toString());
                continue;
            }
            if (code < 200 || code >= 300) {
                closeQuietly(connection.getErrorStream());
                connection.disconnect();
                throw new IOException("Download failed with HTTP " + code + " from " + safeEndpoint(current));
            }
            return connection;
        }
        throw new IOException("Download exceeded the redirect limit.");
    }

    private static boolean canUpgradeOfficialHost(URL url) {
        String host = url.getHost() == null ? "" : url.getHost().toLowerCase(Locale.ENGLISH);
        return host.equals("minecraft.net") || host.endsWith(".minecraft.net")
                || host.equals("mojang.com") || host.endsWith(".mojang.com")
                || host.equals("s3.amazonaws.com");
    }

    private static boolean isRedirect(int code) {
        return code == 301 || code == 302 || code == 303 || code == 307 || code == 308;
    }

    private static void copyBounded(InputStream in, java.io.OutputStream out, long limit) throws IOException {
        byte[] buffer = new byte[8192];
        long total = 0L;
        int read;
        while ((read = in.read(buffer)) != -1) {
            total += read;
            if (total > limit) {
                throw new IOException("Download exceeded the allowed size limit.");
            }
            out.write(buffer, 0, read);
        }
    }

    private static String sha1(File file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            InputStream in = new FileInputStream(file);
            try {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            } finally {
                in.close();
            }
            byte[] bytes = digest.digest();
            StringBuilder out = new StringBuilder(bytes.length * 2);
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(bytes[i] & 0xff);
                if (hex.length() == 1) {
                    out.append('0');
                }
                out.append(hex);
            }
            return out.toString();
        } catch (Exception e) {
            throw new IOException("Could not calculate SHA-1", e);
        }
    }

    private static InputStream safeInputStream(HttpURLConnection connection) {
        try {
            return connection.getInputStream();
        } catch (IOException ignored) {
            return null;
        }
    }

    private static void closeQuietly(InputStream in) {
        if (in == null) {
            return;
        }
        try {
            in.close();
        } catch (IOException ignored) {
        }
    }

    private static boolean empty(String value) {
        return value == null || value.length() == 0;
    }

    private static String safeEndpoint(URL url) {
        if (url == null) {
            return "(unknown host)";
        }
        return url.getProtocol() + "://" + url.getHost()
                + (url.getPath() == null ? "" : url.getPath());
    }
}
