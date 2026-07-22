package net.minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

final class TokenCache {
    private final File dir;
    private final File file;
    private final Properties values = new Properties();

    TokenCache() {
        this(minecraftDir());
    }

    TokenCache(File minecraftRoot) {
        this.dir = new File(minecraftRoot, "launcher_revive");
        this.file = new File(dir, "auth.properties");
        load();
    }

    static File minecraftDir() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH);
        String home = System.getProperty("user.home", ".");
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && appData.trim().length() > 0) {
                return new File(appData, ".minecraft");
            }
        }
        if (os.contains("mac")) {
            return new File(home, "Library/Application Support/minecraft");
        }
        return new File(home, ".minecraft");
    }

    File configFile() {
        return file;
    }

    synchronized String get(String key) {
        return values.getProperty(key);
    }

    synchronized long getLong(String key, long fallback) {
        String value = values.getProperty(key);
        if (value == null) {
            return fallback;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    synchronized boolean hasRefreshToken() {
        String token = values.getProperty("ms.refresh_token");
        return token != null && token.length() > 0;
    }

    synchronized boolean hasFreshMinecraftToken() {
        String token = values.getProperty("mc.access_token");
        String name = values.getProperty("profile.name");
        String id = values.getProperty("profile.id");
        long expiresAt = getLong("mc.expires_at", 0L);
        return token != null && token.length() > 0
                && name != null && name.length() > 0
                && id != null && id.length() > 0
                && System.currentTimeMillis() + 60000L < expiresAt;
    }

    synchronized AuthProfile cachedProfile() {
        if (!hasFreshMinecraftToken()) {
            return null;
        }
        return AuthProfile.online(values.getProperty("profile.name"),
                values.getProperty("profile.id"),
                values.getProperty("mc.access_token"));
    }

    synchronized void put(String key, String value) {
        if (value == null) {
            values.remove(key);
        } else {
            values.setProperty(key, value);
        }
    }

    synchronized void putLong(String key, long value) {
        values.setProperty(key, Long.toString(value));
    }

    synchronized void save() throws IOException {
        SafeFiles.writePropertiesPrivateAtomic(
                file,
                values,
                "MCLauncherRevival token cache. Contains OAuth refresh/access tokens, never raw passwords.");
        hideOnWindows(file);
    }

    synchronized void clear() throws IOException {
        values.clear();
        IOException failure = null;
        if (file.exists() && !file.delete()) {
            failure = new IOException("Could not delete " + file.getAbsolutePath());
        }
        File[] leftovers = dir.listFiles();
        if (leftovers != null) {
            for (int i = 0; i < leftovers.length; i++) {
                String name = leftovers[i].getName();
                if (name.startsWith("auth.properties.") && name.endsWith(".tmp")
                        && leftovers[i].isFile() && !leftovers[i].delete() && failure == null) {
                    failure = new IOException("Could not delete " + leftovers[i].getAbsolutePath());
                }
            }
        }
        try {
            MacLaunchConfig.clearStored(dir);
        } catch (IOException e) {
            if (failure == null) {
                failure = e;
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            values.load(in);
        } catch (IOException ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static void hideOnWindows(File target) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH);
        if (!os.contains("win")) {
            return;
        }
        try {
            new ProcessBuilder("attrib", "+H", target.getAbsolutePath()).start().waitFor();
        } catch (Throwable ignored) {
        }
    }
}
