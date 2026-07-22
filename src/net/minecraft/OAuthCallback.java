package net.minecraft;

import java.io.IOException;
import java.net.URI;

final class OAuthCallback {
    private OAuthCallback() {
    }

    static void requireExpectedRedirect(String returnedUrl, String expectedRedirect) throws IOException {
        try {
            URI returned = new URI(returnedUrl == null ? "" : returnedUrl.trim());
            URI expected = new URI(expectedRedirect == null ? "" : expectedRedirect.trim());
            if (!sameIgnoreCase(returned.getScheme(), expected.getScheme())
                    || !sameIgnoreCase(returned.getHost(), expected.getHost())
                    || effectivePort(returned) != effectivePort(expected)
                    || !normalizePath(returned.getPath()).equals(normalizePath(expected.getPath()))
                    || returned.getUserInfo() != null
                    || returned.getFragment() != null) {
                throw new IOException("Microsoft login returned an unexpected redirect address.");
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Microsoft login returned an invalid redirect address.");
        }
    }

    private static int effectivePort(URI uri) {
        if (uri.getPort() >= 0) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private static String normalizePath(String value) {
        return value == null || value.length() == 0 ? "/" : value;
    }

    private static boolean sameIgnoreCase(String left, String right) {
        return left == null ? right == null : right != null && left.equalsIgnoreCase(right);
    }
}
