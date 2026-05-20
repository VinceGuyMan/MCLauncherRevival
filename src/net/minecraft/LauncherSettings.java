package net.minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

final class LauncherSettings {
    private final File dir;
    private final File file;
    private final Properties values = new Properties();

    LauncherSettings() {
        this.dir = new File(TokenCache.minecraftDir(), "launcher_revive");
        this.file = new File(dir, "launcher.properties");
        load();
    }

    String get(String key, String fallback) {
        String value = values.getProperty(key);
        return value == null ? fallback : value;
    }

    boolean getBoolean(String key, boolean fallback) {
        String value = values.getProperty(key);
        if (value == null) {
            return fallback;
        }
        return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    }

    void put(String key, String value) {
        if (value == null) {
            values.remove(key);
        } else {
            values.setProperty(key, value);
        }
    }

    void putBoolean(String key, boolean value) {
        values.setProperty(key, Boolean.toString(value));
    }

    void save() throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create " + dir.getAbsolutePath());
        }
        File temp = new File(dir, "launcher.properties.tmp");
        FileOutputStream out = new FileOutputStream(temp);
        try {
            values.store(out, "MCLauncherRevival launcher settings.");
        } finally {
            out.close();
        }
        if (file.exists() && !file.delete()) {
            throw new IOException("Could not replace " + file.getAbsolutePath());
        }
        if (!temp.renameTo(file)) {
            throw new IOException("Could not write " + file.getAbsolutePath());
        }
    }

    File file() {
        return file;
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
}

