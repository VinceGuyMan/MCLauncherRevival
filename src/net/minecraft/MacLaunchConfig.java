package net.minecraft;

import java.io.File;
import java.io.IOException;

final class MacLaunchConfig {
    private static final String FILE_NAME = "game-launch.properties";

    private MacLaunchConfig() {
    }

    static File write(
            File runtimeDir,
            File logFile,
            String javaCommand,
            String memory,
            File nativeDir,
            String classpath,
            File gameDir,
            AuthProfile profile,
            String version) throws IOException {
        SafeFiles.ensureDirectory(runtimeDir);
        File config = new File(runtimeDir, FILE_NAME);
        StringBuilder out = new StringBuilder();
        append(out, "java", javaCommand);
        append(out, "memory", memory);
        append(out, "nativeDir", nativeDir.getAbsolutePath());
        append(out, "classpath", classpath);
        append(out, "log", logFile.getAbsolutePath());
        append(out, "username", profile.name);
        append(out, "session", profile.sessionId());
        append(out, "gameDir", gameDir.getAbsolutePath());
        append(out, "version", version);
        SafeFiles.writeUtf8PrivateAtomic(config, out.toString());
        return config;
    }

    static void clearStored(File launcherDataDir) throws IOException {
        if (launcherDataDir == null) {
            return;
        }
        File runtimeDir = new File(launcherDataDir, "runtime");
        File config = new File(runtimeDir, FILE_NAME);
        if (config.exists() && !config.delete()) {
            throw new IOException("Could not delete stored macOS launch credentials: " + config.getAbsolutePath());
        }
        File[] files = runtimeDir.listFiles();
        if (files == null) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if (name.startsWith(FILE_NAME + ".") && name.endsWith(".tmp")
                    && files[i].isFile() && !files[i].delete()) {
                throw new IOException("Could not delete temporary macOS launch credentials: "
                        + files[i].getAbsolutePath());
            }
        }
    }

    private static void append(StringBuilder out, String key, String value) {
        out.append(key).append('=').append(clean(value)).append('\n');
    }

    private static String clean(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ');
    }
}
