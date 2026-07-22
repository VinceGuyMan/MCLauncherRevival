package net.minecraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class LauncherSelfTest {
    private int checks;

    public static void main(String[] args) throws Exception {
        LauncherSelfTest test = new LauncherSelfTest();
        test.run();
        System.out.println("Launcher self-tests passed: " + test.checks + " checks.");
    }

    private void run() throws Exception {
        testJsonParser();
        testVersionIdsAndContainedPaths();
        testDownloadUrlPolicy();
        testOAuthRedirectValidation();
        testAtomicReplace();
        testNativeExtraction();
        testTokenAndLaunchSecretCleanup();
        testOfflineProfile();
    }

    private void testJsonParser() throws Exception {
        Map<String, Object> parsed = Json.object(Json.parse("{\"name\":\"beta\",\"count\":3,\"ok\":true}"));
        equal("beta", Json.string(parsed, "name"), "JSON string value");
        equal(Long.valueOf(3L), Long.valueOf(Json.number(parsed, "count", 0L)), "JSON numeric value");
        expectIOException(new CheckedAction() {
            public void run() throws Exception {
                Json.parse("{\"broken\":]");
            }
        }, "malformed JSON is rejected");
    }

    private void testVersionIdsAndContainedPaths() throws Exception {
        truth(SafeFiles.isSafeVersionId("b1.7.3"), "Beta version id is accepted");
        truth(SafeFiles.isSafeVersionId("a1.2.6_02"), "Alpha version id is accepted");
        truth(!SafeFiles.isSafeVersionId("../../escape"), "path-like version id is rejected");
        truth(!SafeFiles.isSafeVersionId("bad/version"), "version separator is rejected");

        final File root = tempDirectory("mclr-path-test");
        try {
            File child = SafeFiles.resolveInside(root, "org/lwjgl/lwjgl.jar", "test path");
            truth(child.getCanonicalPath().startsWith(root.getCanonicalPath() + File.separator),
                    "nested library path stays inside root");
            expectIOException(new CheckedAction() {
                public void run() throws Exception {
                    SafeFiles.resolveInside(root, "../../outside.jar", "test path");
                }
            }, "traversal path is rejected");
        } finally {
            deleteRecursively(root);
        }
    }

    private void testDownloadUrlPolicy() throws Exception {
        equal("https", DownloadClient.validatedUrl("https://launchermeta.mojang.com/test").getProtocol(),
                "HTTPS URL is accepted");
        equal("https", DownloadClient.validatedUrl("http://textures.minecraft.net/texture/test").getProtocol(),
                "known Mojang HTTP URL is upgraded");
        expectIOException(new CheckedAction() {
            public void run() throws Exception {
                DownloadClient.validatedUrl("http://example.invalid/file.jar");
            }
        }, "unknown HTTP URL is rejected");
        expectIOException(new CheckedAction() {
            public void run() throws Exception {
                DownloadClient.validatedUrl("file:///tmp/file.jar");
            }
        }, "local file URL is rejected");
        expectIOException(new CheckedAction() {
            public void run() throws Exception {
                DownloadClient.validatedUrl("https://user:secret@example.invalid/file.jar");
            }
        }, "URL credentials are rejected");
    }

    private void testOAuthRedirectValidation() throws Exception {
        OAuthCallback.requireExpectedRedirect(
                "https://login.live.com/oauth20_desktop.srf?code=sample&state=state",
                "https://login.live.com/oauth20_desktop.srf");
        checks++;
        expectIOException(new CheckedAction() {
            public void run() throws Exception {
                OAuthCallback.requireExpectedRedirect(
                        "https://example.invalid/?next=https://login.live.com/oauth20_desktop.srf&code=sample",
                        "https://login.live.com/oauth20_desktop.srf");
            }
        }, "lookalike OAuth redirect is rejected");
        expectIOException(new CheckedAction() {
            public void run() throws Exception {
                OAuthCallback.requireExpectedRedirect(
                        "http://127.0.0.1:54322/mclauncherrevival/oauth?code=sample",
                        "http://127.0.0.1:54321/mclauncherrevival/oauth");
            }
        }, "wrong loopback callback port is rejected");
        expectIOException(new CheckedAction() {
            public void run() throws Exception {
                OAuthCallback.requireExpectedRedirect(
                        "https://login.live.com/OAUTH20_DESKTOP.SRF?code=sample",
                        "https://login.live.com/oauth20_desktop.srf");
            }
        }, "OAuth redirect path is matched exactly");
        expectIOException(new CheckedAction() {
            public void run() throws Exception {
                OAuthCallback.requireExpectedRedirect(
                        "https://login.live.com/oauth20_desktop.srf#code=sample",
                        "https://login.live.com/oauth20_desktop.srf");
            }
        }, "OAuth redirect fragment is rejected");
    }

    private void testAtomicReplace() throws Exception {
        File root = tempDirectory("mclr-atomic-test");
        try {
            File target = new File(root, "settings.txt");
            SafeFiles.writeUtf8Atomic(target, "first");
            SafeFiles.writeUtf8Atomic(target, "second");
            equal("second", readString(target), "atomic write replaces complete file");
        } finally {
            deleteRecursively(root);
        }
    }

    private void testNativeExtraction() throws Exception {
        File root = tempDirectory("mclr-native-test");
        try {
            File archive = new File(root, "natives.jar");
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(archive));
            try {
                writeZipEntry(zip, "nested/libsafe.dylib", "safe");
                writeZipEntry(zip, "../escape.dylib", "unsafe");
                writeZipEntry(zip, "META-INF/signature.SF", "signature");
            } finally {
                zip.close();
            }
            File destination = new File(root, "out");
            NativeExtractor.extractFlat(archive, destination);
            truth(new File(destination, "libsafe.dylib").isFile(), "native is extracted by basename");
            truth(!new File(root, "escape.dylib").exists(), "native traversal entry is skipped");
            truth(!new File(destination, "signature.SF").exists(), "META-INF entry is skipped");
        } finally {
            deleteRecursively(root);
        }
    }

    private void testTokenAndLaunchSecretCleanup() throws Exception {
        File root = tempDirectory("mclr-token-test");
        try {
            TokenCache cache = new TokenCache(root);
            cache.put("ms.refresh_token", "refresh-secret");
            cache.put("mc.access_token", "minecraft-secret");
            cache.save();
            truth(cache.configFile().isFile(), "token cache is written");

            File launcherDir = new File(root, "launcher_revive");
            File runtimeDir = new File(launcherDir, "runtime");
            File config = MacLaunchConfig.write(
                    runtimeDir,
                    new File(launcherDir, "logs/last-launch.log"),
                    "/java/bin/java",
                    "512",
                    new File(root, "versions/b1.7.3/natives"),
                    "client.jar",
                    root,
                    AuthProfile.online("Player", "0123456789abcdef", "minecraft-secret"),
                    "b1.7.3");
            truth(config.isFile(), "temporary macOS launch config is written");
            truth(readString(config).indexOf("minecraft-secret") >= 0, "test launch config contains session before cleanup");

            File tokenTemp = new File(launcherDir, "auth.properties.crash.tmp");
            File launchTemp = new File(runtimeDir, "game-launch.properties.crash.tmp");
            writeString(tokenTemp, "temporary-token");
            writeString(launchTemp, "temporary-session");

            cache.clear();
            truth(!cache.configFile().exists(), "Forget Login removes token cache");
            truth(!config.exists(), "Forget Login removes macOS launch credentials");
            truth(!tokenTemp.exists(), "Forget Login removes interrupted token writes");
            truth(!launchTemp.exists(), "Forget Login removes interrupted launch-config writes");
        } finally {
            deleteRecursively(root);
        }
    }

    private void testOfflineProfile() {
        AuthProfile first = AuthProfile.offline("Bad Name!");
        AuthProfile second = AuthProfile.offline("Bad Name!");
        equal("Bad_Name_", first.name, "offline name is sanitized");
        equal(first.uuid, second.uuid, "offline UUID is deterministic");
        equal("-", first.sessionId(), "offline session is not a token");
    }

    private void truth(boolean value, String label) {
        checks++;
        if (!value) {
            throw new AssertionError(label);
        }
    }

    private void equal(Object expected, Object actual, String label) {
        checks++;
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }

    private void expectIOException(CheckedAction action, String label) throws Exception {
        checks++;
        try {
            action.run();
        } catch (IOException expected) {
            return;
        }
        throw new AssertionError(label);
    }

    private static File tempDirectory(String prefix) throws IOException {
        File file = File.createTempFile(prefix, "");
        if (!file.delete() || !file.mkdirs()) {
            throw new IOException("Could not create test folder " + file.getAbsolutePath());
        }
        return file;
    }

    private static void writeZipEntry(ZipOutputStream zip, String name, String value) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(value.getBytes("UTF-8"));
        zip.closeEntry();
    }

    private static String readString(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder out = new StringBuilder();
        try {
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                out.append(buffer, 0, read);
            }
        } finally {
            reader.close();
        }
        return out.toString();
    }

    private static void writeString(File file, String value) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(value.getBytes("UTF-8"));
        } finally {
            out.close();
        }
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    deleteRecursively(children[i]);
                }
            }
        }
        file.delete();
    }

    private interface CheckedAction {
        void run() throws Exception;
    }
}
