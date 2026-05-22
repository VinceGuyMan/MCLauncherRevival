package net.minecraft;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.net.ssl.SSLHandshakeException;

final class BetaLauncher {
    static final String DEFAULT_VERSION = "b1.7.3";
    private static final String MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    private final String version;
    private final String memoryMegabytes;
    private final StatusSink status;

    BetaLauncher(StatusSink status) {
        this(DEFAULT_VERSION, status);
    }

    BetaLauncher(String version, StatusSink status) {
        this(version, "1024", status);
    }

    BetaLauncher(String version, String memoryMegabytes, StatusSink status) {
        this.version = normalizeVersion(version);
        this.memoryMegabytes = normalizeMemory(memoryMegabytes);
        this.status = status;
    }

    static List<String> loadLegacyVersions() throws IOException {
        String manifest = downloadString(MANIFEST_URL);
        Map<String, Object> manifestJson = Json.object(Json.parse(manifest));
        List<Object> versions = Json.array(manifestJson.get("versions"));
        ArrayList<String> result = new ArrayList<String>();
        boolean include = false;
        if (versions == null) {
            result.add(DEFAULT_VERSION);
            return result;
        }
        for (Object value : versions) {
            Map<String, Object> entry = Json.object(value);
            String id = Json.string(entry, "id");
            String type = Json.string(entry, "type");
            if (id == null) {
                continue;
            }
            if ("b1.8.1".equals(id) || "b1.8".equals(id)) {
                include = true;
            }
            if (include && ("old_beta".equals(type) || "old_alpha".equals(type))) {
                result.add(id);
            }
        }
        if (result.size() == 0) {
            for (Object value : versions) {
                Map<String, Object> entry = Json.object(value);
                String id = Json.string(entry, "id");
                String type = Json.string(entry, "type");
                if (id != null && ("old_beta".equals(type) || "old_alpha".equals(type))) {
                    result.add(id);
                }
            }
        }
        if (!result.contains(DEFAULT_VERSION)) {
            result.add(DEFAULT_VERSION);
        }
        return result;
    }

    static String downloadStringForLauncher(String url) throws IOException {
        return downloadString(url);
    }

    static String xpVersionFilesMessage() {
        return "Windows XP offline mode needs the selected Minecraft version files to already be present. "
                + "This PC could not download them because XP/Java 7 cannot reliably connect to modern HTTPS services. "
                + "Launch this version once on Windows 7 or newer, then copy your .minecraft versions, libraries, "
                + "and assets folders to this XP machine.\n\n"
                + "Newer Windows source:\n"
                + "%APPDATA%\\.minecraft\\versions\n"
                + "%APPDATA%\\.minecraft\\libraries\n"
                + "%APPDATA%\\.minecraft\\assets\n\n"
                + "Windows XP destination:\n"
                + "C:\\Documents and Settings\\<User>\\Application Data\\.minecraft\\versions\n"
                + "C:\\Documents and Settings\\<User>\\Application Data\\.minecraft\\libraries\n"
                + "C:\\Documents and Settings\\<User>\\Application Data\\.minecraft\\assets\n\n"
                + "Use only Minecraft files you own or otherwise have the right to use.";
    }

    static String xpLooseJarMessage(String version) {
        String clean = normalizeVersion(version);
        return "A loose " + clean + ".jar was found, but MCLauncherRevival needs the Mojang launcher-style "
                + "version folder. Move/copy the jar to .minecraft\\versions\\" + clean + "\\" + clean
                + ".jar and provide the matching " + clean + ".json, libraries, and assets. The easiest fix "
                + "is to launch this version once on Windows 7 or newer, then copy the full .minecraft "
                + "versions, libraries, and assets folders to XP.";
    }

    static boolean isXpHttpsFailure(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof SSLHandshakeException) {
                return true;
            }
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase(Locale.ENGLISH);
                if (lower.indexOf("handshake_failure") >= 0
                        || lower.indexOf("received fatal alert") >= 0
                        || lower.indexOf("pkix") >= 0
                        || lower.indexOf("unable to find valid certification path") >= 0) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    static String memoryPreview(String value) {
        return normalizeMemory(value);
    }

    void launch(AuthProfile profile) throws IOException {
        File minecraftDir = TokenCache.minecraftDir();
        File versionDir = new File(new File(minecraftDir, "versions"), version);
        File librariesDir = new File(minecraftDir, "libraries");
        File nativeDir = new File(versionDir, "natives");
        File gameDir = minecraftDir;
        File logDir = new File(new File(minecraftDir, "launcher_revive"), "logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        status.status("Preparing " + version + " files...");
        Map<String, Object> versionJson = xpCompatibilityMode()
                ? loadLocalVersionJsonForXp(versionDir, librariesDir)
                : loadVersionJson(versionDir);
        String mainClass = Json.string(versionJson, "mainClass");
        if (mainClass == null || mainClass.length() == 0) {
            mainClass = "net.minecraft.client.Minecraft";
        }

        ArrayList<File> classpath = new ArrayList<File>();
        classpath.add(downloadClientJar(versionJson, versionDir));
        downloadLibraries(versionJson, librariesDir, nativeDir, classpath);

        String minecraftArguments = Json.string(versionJson, "minecraftArguments");
        if (minecraftArguments == null || minecraftArguments.trim().length() == 0) {
            minecraftArguments = "${auth_player_name} ${auth_session}";
        }

        ArrayList<String> command = new ArrayList<String>();
        command.add(javaExecutable());
        command.add("-Xmx" + memoryMegabytes + "M");
        command.add("-Djava.library.path=" + nativeDir.getAbsolutePath());
        command.add("-cp");
        command.add(joinClasspath(classpath));
        command.add(mainClass);
        command.addAll(expandArguments(minecraftArguments, profile, gameDir));

        File logFile = new File(logDir, "last-launch.log");
        status.status(
                "Starting Minecraft "
                        + version
                        + " as "
                        + profile.name
                        + (profile.online ? " (online token)." : " (offline mode)."));
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(gameDir);
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
        builder.start();
        status.status("Minecraft started. Launch log: " + logFile.getAbsolutePath());
    }

    private Map<String, Object> loadVersionJson(File versionDir) throws IOException {
        File jsonFile = new File(versionDir, version + ".json");
        if (!jsonFile.exists()) {
            if (xpCompatibilityMode()) {
                throw new IOException(xpVersionFilesMessage());
            }
            if (!versionDir.exists() && !versionDir.mkdirs()) {
                throw new IOException("Could not create " + versionDir.getAbsolutePath());
            }
            status.status("Downloading Mojang version manifest...");
            String manifest = downloadString(MANIFEST_URL);
            Map<String, Object> manifestJson = Json.object(Json.parse(manifest));
            List<Object> versions = Json.array(manifestJson.get("versions"));
            String versionUrl = null;
            if (versions != null) {
                for (Object value : versions) {
                    Map<String, Object> entry = Json.object(value);
                    if (version.equals(Json.string(entry, "id"))) {
                        versionUrl = Json.string(entry, "url");
                        break;
                    }
                }
            }
            if (versionUrl == null) {
                throw new IOException("Could not find " + version + " in Mojang version manifest.");
            }
            status.status("Downloading " + version + " metadata...");
            writeString(jsonFile, downloadString(versionUrl));
        }
        return Json.object(Json.parse(readString(jsonFile)));
    }

    private static File localVersionJar(File versionDir, String version) {
        File exact = new File(versionDir, version + ".jar");
        if (exact.exists()) {
            return exact;
        }
        return singleFileWithExtension(versionDir, ".jar");
    }

    private static File localVersionJson(File versionDir, String version) {
        File exact = new File(versionDir, version + ".json");
        if (exact.exists()) {
            return exact;
        }
        return singleFileWithExtension(versionDir, ".json");
    }

    private static File singleFileWithExtension(File dir, String extension) {
        if (dir == null || !dir.isDirectory()) {
            return null;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        File found = null;
        String lowerExtension = extension.toLowerCase(Locale.ENGLISH);
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file == null || !file.isFile()) {
                continue;
            }
            String name = file.getName().toLowerCase(Locale.ENGLISH);
            if (!name.endsWith(lowerExtension)) {
                continue;
            }
            if (found != null) {
                return null;
            }
            found = file;
        }
        return found;
    }
    private Map<String, Object> loadLocalVersionJsonForXp(File versionDir, File librariesDir) throws IOException {
        File jarFile = localVersionJar(versionDir, version);
        File jsonFile = localVersionJson(versionDir, version);
        if (jarFile == null || !jarFile.exists() || jsonFile == null || !jsonFile.exists()) {
            File versionsDir = versionDir.getParentFile();
            File looseJar = versionsDir == null ? null : new File(versionsDir, version + ".jar");
            if (looseJar != null && looseJar.exists()) {
                throw new IOException(xpLooseJarMessage(version));
            }
            throw new IOException(xpVersionFilesMessage());
        }
        Map<String, Object> versionJson = Json.object(Json.parse(readString(jsonFile)));
        ensureXpLibrariesPresent(versionJson, librariesDir);
        return versionJson;
    }

    private void ensureXpLibrariesPresent(Map<String, Object> versionJson, File librariesDir) throws IOException {
        List<Object> libraries = Json.array(versionJson.get("libraries"));
        if (libraries == null) {
            return;
        }
        for (Object value : libraries) {
            Map<String, Object> library = Json.object(value);
            if (!allowed(library)) {
                continue;
            }
            Map<String, Object> downloads = Json.object(library.get("downloads"));
            Map<String, Object> artifact = Json.object(downloads == null ? null : downloads.get("artifact"));
            if (artifact != null) {
                requireXpFile(libraryFile(librariesDir, artifact, Json.string(library, "name"), null));
            } else if (Json.object(library.get("natives")) == null && Json.string(library, "name") != null) {
                requireXpFile(libraryFile(librariesDir, null, Json.string(library, "name"), null));
            }

            Map<String, Object> natives = Json.object(library.get("natives"));
            if (natives != null) {
                String classifier = Json.string(natives, osName());
                if (classifier != null) {
                    classifier = classifier.replace("${arch}", is64Bit() ? "64" : "32");
                    Map<String, Object> classifiers =
                            Json.object(downloads == null ? null : downloads.get("classifiers"));
                    Map<String, Object> nativeArtifact =
                            Json.object(classifiers == null ? null : classifiers.get(classifier));
                    requireXpFile(libraryFile(librariesDir, nativeArtifact, Json.string(library, "name"), classifier));
                }
            }
        }
    }

    private static void requireXpFile(File file) throws IOException {
        if (file == null || !file.exists()) {
            String path = file == null ? "(unknown file)" : file.getAbsolutePath();
            throw new IOException("Missing required Minecraft file:\n" + path + "\n\n" + xpVersionFilesMessage());
        }
    }

    private File downloadClientJar(Map<String, Object> versionJson, File versionDir) throws IOException {
        File localJar = localVersionJar(versionDir, version);
        File jar = localJar == null ? new File(versionDir, version + ".jar") : localJar;
        Map<String, Object> downloads = Json.object(versionJson.get("downloads"));
        Map<String, Object> client = Json.object(downloads == null ? null : downloads.get("client"));
        String url = Json.string(client, "url");
        String sha1 = Json.string(client, "sha1");
        if (url == null) {
            if (xpCompatibilityMode() && jar.exists()) {
                return jar;
            }
            throw missingGameFile("client jar", jar,
                    new IOException("Version metadata did not include a client jar URL."));
        }
        try {
            downloadFile(url, jar, sha1);
        } catch (IOException ex) {
            throw missingGameFile("client jar", jar, ex);
        }
        return jar;
    }

    private void downloadLibraries(
            Map<String, Object> versionJson,
            File librariesDir,
            File nativeDir,
            List<File> classpath) throws IOException {
        List<Object> libraries = Json.array(versionJson.get("libraries"));
        if (libraries == null) {
            return;
        }
        if (!nativeDir.exists()) {
            nativeDir.mkdirs();
        }
        for (Object value : libraries) {
            Map<String, Object> library = Json.object(value);
            if (!allowed(library)) {
                continue;
            }
            Map<String, Object> downloads = Json.object(library.get("downloads"));
            Map<String, Object> artifact = Json.object(downloads == null ? null : downloads.get("artifact"));
            if (artifact != null) {
                File artifactFile = libraryFile(librariesDir, artifact, Json.string(library, "name"), null);
                try {
                    downloadFile(artifactUrl(library, artifact, null), artifactFile, Json.string(artifact, "sha1"));
                } catch (IOException ex) {
                    throw missingGameFile("library", artifactFile, ex);
                }
                classpath.add(artifactFile);
            } else if (Json.object(library.get("natives")) == null && Json.string(library, "name") != null) {
                File artifactFile = libraryFile(librariesDir, null, Json.string(library, "name"), null);
                try {
                    downloadFile(artifactUrl(library, null, null), artifactFile, null);
                } catch (IOException ex) {
                    throw missingGameFile("library", artifactFile, ex);
                }
                classpath.add(artifactFile);
            }

            Map<String, Object> natives = Json.object(library.get("natives"));
            if (natives != null) {
                String classifier = Json.string(natives, osName());
                if (classifier != null) {
                    classifier = classifier.replace("${arch}", is64Bit() ? "64" : "32");
                    Map<String, Object> classifiers =
                            Json.object(downloads == null ? null : downloads.get("classifiers"));
                    Map<String, Object> nativeArtifact =
                            Json.object(classifiers == null ? null : classifiers.get(classifier));
                    File nativeJar =
                            libraryFile(librariesDir, nativeArtifact, Json.string(library, "name"), classifier);
                    try {
                        downloadFile(
                                artifactUrl(library, nativeArtifact, classifier),
                                nativeJar,
                                Json.string(nativeArtifact, "sha1"));
                        extractNatives(nativeJar, nativeDir);
                    } catch (IOException ex) {
                        throw missingGameFile("native", nativeJar, ex);
                    }
                }
            }
        }
    }

    private static boolean allowed(Map<String, Object> library) {
        List<Object> rules = Json.array(library.get("rules"));
        if (rules == null || rules.size() == 0) {
            return true;
        }
        boolean allowed = false;
        for (Object value : rules) {
            Map<String, Object> rule = Json.object(value);
            String action = Json.string(rule, "action");
            Map<String, Object> os = Json.object(rule.get("os"));
            boolean matches = os == null;
            if (os != null) {
                String name = Json.string(os, "name");
                matches = name == null || name.equals(osName());
            }
            if (matches) {
                allowed = "allow".equals(action);
            }
        }
        return allowed;
    }

    private IOException missingGameFile(String kind, File file, IOException cause) {
        String path = file == null ? "(unknown file)" : file.getAbsolutePath();
        StringBuilder message = new StringBuilder();
        message.append("Missing or unavailable Minecraft ").append(kind).append(" file:\n");
        message.append(path).append("\n\n");
        message.append("Minecraft ").append(version).append(" needs its client jar, JSON metadata, libraries, ");
        message.append("LWJGL natives, and sometimes assets to be present before launch.\n\n");
        message.append("If this PC is online and supported, try Play or Redownload Version again. ");
        message.append("On Windows XP, prepare this version on Windows 7 or newer, then copy your ");
        message.append(".minecraft versions, libraries, and assets folders to the XP machine.");
        if (cause != null && cause.getMessage() != null && cause.getMessage().length() > 0) {
            message.append("\n\nOriginal reason: ").append(cause.getMessage());
        }
        return new IOException(message.toString(), cause);
    }

    private static File libraryFile(
            File librariesDir,
            Map<String, Object> artifact,
            String name,
            String classifier) throws IOException {
        String path = Json.string(artifact, "path");
        if (path == null) {
            path = pathFromMavenName(name, classifier);
        }
        if (path == null) {
            throw new IOException("Could not resolve library path for " + name);
        }
        return new File(librariesDir, path.replace('/', File.separatorChar));
    }

    private static String artifactUrl(Map<String, Object> library, Map<String, Object> artifact, String classifier) {
        String url = Json.string(artifact, "url");
        if (url != null && url.length() > 0) {
            return url;
        }
        String path = Json.string(artifact, "path");
        if (path == null) {
            path = pathFromMavenName(Json.string(library, "name"), classifier);
        }
        if (path == null) {
            return null;
        }
        String base = Json.string(library, "url");
        if (base == null || base.length() == 0) {
            base = "https://libraries.minecraft.net/";
        }
        return base + (base.endsWith("/") ? "" : "/") + path;
    }

    private static String pathFromMavenName(String name, String classifier) {
        if (name == null) {
            return null;
        }
        String[] parts = name.split(":");
        if (parts.length < 3) {
            return null;
        }
        String group = parts[0].replace('.', '/');
        String artifact = parts[1];
        String version = parts[2];
        String file = artifact + "-" + version + (classifier == null ? "" : "-" + classifier) + ".jar";
        return group + "/" + artifact + "/" + version + "/" + file;
    }

    private static void extractNatives(File nativeJar, File nativeDir) throws IOException {
        ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(nativeJar)));
        try {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = in.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory() || name.startsWith("META-INF/") || name.contains("..")) {
                    continue;
                }
                File outFile = new File(nativeDir, new File(name).getName());
                FileOutputStream out = new FileOutputStream(outFile);
                try {
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                } finally {
                    out.close();
                }
            }
        } finally {
            in.close();
        }
    }

    private List<String> expandArguments(String raw, AuthProfile profile, File gameDir) {
        String assets = new File(TokenCache.minecraftDir(), "assets").getAbsolutePath();
        String expanded = raw
                .replace("${auth_player_name}", profile.name)
                .replace("${auth_session}", profile.sessionId())
                .replace("${auth_uuid}", profile.uuid)
                .replace("${auth_access_token}", profile.accessToken)
                .replace("${user_type}", profile.online ? "msa" : "legacy")
                .replace("${user_properties}", "{}")
                .replace("${version_name}", version)
                .replace("${game_directory}", gameDir.getAbsolutePath())
                .replace("${assets_root}", assets)
                .replace("${game_assets}", assets)
                .replace("${assets_index_name}", "legacy");
        return splitArguments(expanded);
    }

    private static List<String> splitArguments(String raw) {
        ArrayList<String> args = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        char quote = 0;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if ((c == '"' || c == '\'') && (!quoted || quote == c)) {
                quoted = !quoted;
                quote = quoted ? c : 0;
            } else if (Character.isWhitespace(c) && !quoted) {
                if (current.length() > 0) {
                    args.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            args.add(current.toString());
        }
        return args;
    }

    private static void downloadFile(String url, File file, String sha1) throws IOException {
        if (file.exists() && (sha1 == null || sha1.length() == 0 || sha1(file).equalsIgnoreCase(sha1))) {
            return;
        }
        if (xpCompatibilityMode()) {
            throw new IOException(xpVersionFilesMessage());
        }
        if (url == null || url.length() == 0) {
            throw new IOException("Missing download URL for Minecraft file: " + file.getAbsolutePath());
        }
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        File temp = new File(file.getAbsolutePath() + ".tmp");
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "MCLauncherRevival/0.1-alpha");
            InputStream in = connection.getInputStream();
            FileOutputStream out = new FileOutputStream(temp);
            try {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            } finally {
                out.close();
                in.close();
            }
        } catch (IOException e) {
            temp.delete();
            if (xpCompatibilityMode() && isXpHttpsFailure(e)) {
                throw new IOException(xpVersionFilesMessage(), e);
            }
            throw e;
        }
        if (sha1 != null && sha1.length() > 0 && !sha1(temp).equalsIgnoreCase(sha1)) {
            temp.delete();
            throw new IOException("SHA-1 check failed for " + file.getName());
        }
        if (file.exists()) {
            file.delete();
        }
        if (!temp.renameTo(file)) {
            throw new IOException("Could not move " + temp.getAbsolutePath() + " to " + file.getAbsolutePath());
        }
    }

    private static String downloadString(String url) throws IOException {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "MCLauncherRevival/0.1-alpha");
            return readAll(connection.getInputStream());
        } catch (IOException e) {
            if (xpCompatibilityMode() && isXpHttpsFailure(e)) {
                throw new IOException(xpVersionFilesMessage(), e);
            }
            throw e;
        }
    }

    private static String readAll(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder out = new StringBuilder();
        char[] buffer = new char[4096];
        int read;
        try {
            while ((read = reader.read(buffer)) != -1) {
                out.append(buffer, 0, read);
            }
        } finally {
            reader.close();
        }
        return out.toString();
    }

    private static String readString(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder out = new StringBuilder();
        char[] buffer = new char[4096];
        int read;
        try {
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

    private static String joinClasspath(List<File> files) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < files.size(); i++) {
            if (i > 0) {
                out.append(File.pathSeparatorChar);
            }
            out.append(files.get(i).getAbsolutePath());
        }
        return out.toString();
    }

    private static String javaExecutable() {
        String home = System.getProperty("java.home");
        String exe = osName().equals("windows") ? "javaw.exe" : "java";
        File java = new File(new File(home, "bin"), exe);
        return java.exists() ? java.getAbsolutePath() : exe;
    }

    private static String osName() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH);
        if (os.contains("win")) {
            return "windows";
        }
        if (os.contains("mac")) {
            return "osx";
        }
        if (os.contains("linux")) {
            return "linux";
        }
        return os;
    }

    private static boolean is64Bit() {
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ENGLISH);
        return arch.contains("64");
    }

    private static boolean xpCompatibilityMode() {
        return Boolean.getBoolean("mclauncher.xpMode");
    }

    private static String normalizeVersion(String value) {
        if (value == null || value.trim().length() == 0) {
            return DEFAULT_VERSION;
        }
        return value.trim();
    }

    private static String normalizeMemory(String value) {
        if (value == null) {
            return "1024";
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.length() == 0) {
            return "1024";
        }
        try {
            int parsed = Integer.parseInt(digits);
            if (parsed < 64) {
                return "64";
            }
            return Integer.toString(parsed);
        } catch (NumberFormatException e) {
            return "1024";
        }
    }
}

