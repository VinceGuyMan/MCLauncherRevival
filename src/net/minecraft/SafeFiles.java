package net.minecraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

final class SafeFiles {
    private static final int MAX_VERSION_ID_LENGTH = 128;

    private SafeFiles() {
    }

    static boolean isSafeVersionId(String value) {
        if (value == null) {
            return false;
        }
        String clean = value.trim();
        if (clean.length() == 0 || clean.length() > MAX_VERSION_ID_LENGTH
                || ".".equals(clean) || "..".equals(clean)) {
            return false;
        }
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if (!((c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == '.' || c == '_' || c == '-')) {
                return false;
            }
        }
        return true;
    }

    static String requireSafeVersionId(String value) throws IOException {
        String clean = value == null ? "" : value.trim();
        if (!isSafeVersionId(clean)) {
            throw new IOException("Invalid Minecraft version id: " + safeDisplay(clean)
                    + ". Use only letters, numbers, dots, underscores, and hyphens.");
        }
        return clean;
    }

    static File resolveInside(File root, String relative, String label) throws IOException {
        if (root == null) {
            throw new IOException("Missing root folder for " + label + ".");
        }
        if (relative == null || relative.trim().length() == 0 || relative.indexOf('\0') >= 0) {
            throw new IOException("Missing or invalid " + label + ".");
        }
        String normalized = relative.replace('\\', '/');
        if (normalized.startsWith("/") || new File(relative).isAbsolute()) {
            throw new IOException("Refusing absolute " + label + ": " + safeDisplay(relative));
        }
        String[] parts = normalized.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].length() == 0 || ".".equals(parts[i]) || "..".equals(parts[i])) {
                throw new IOException("Refusing unsafe " + label + ": " + safeDisplay(relative));
            }
        }

        File canonicalRoot = root.getCanonicalFile();
        File target = new File(canonicalRoot, normalized.replace('/', File.separatorChar)).getCanonicalFile();
        String rootPath = canonicalRoot.getPath();
        String targetPath = target.getPath();
        if (target.equals(canonicalRoot) || !targetPath.startsWith(rootPath + File.separator)) {
            throw new IOException("Refusing " + label + " outside " + canonicalRoot.getAbsolutePath());
        }
        return target;
    }

    static void ensureDirectory(File dir) throws IOException {
        if (dir == null) {
            throw new IOException("Missing destination folder.");
        }
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException("Expected a folder at " + dir.getAbsolutePath());
            }
            return;
        }
        if (!dir.mkdirs() && !dir.isDirectory()) {
            throw new IOException("Could not create " + dir.getAbsolutePath());
        }
    }

    static void writeUtf8Atomic(File target, String value) throws IOException {
        writeBytesAtomic(target, value == null ? new byte[0] : value.getBytes("UTF-8"), false);
    }

    static void writeUtf8PrivateAtomic(File target, String value) throws IOException {
        writeBytesAtomic(target, value == null ? new byte[0] : value.getBytes("UTF-8"), true);
    }

    static void writePropertiesPrivateAtomic(File target, Properties values, String comment) throws IOException {
        File parent = target.getParentFile();
        ensureDirectory(parent);
        File temp = File.createTempFile(target.getName() + ".", ".tmp", parent);
        boolean moved = false;
        try {
            restrictOwner(temp);
            FileOutputStream out = new FileOutputStream(temp);
            try {
                values.store(out, comment);
                out.getFD().sync();
            } finally {
                out.close();
            }
            restrictOwner(temp);
            atomicReplace(temp, target);
            moved = true;
            restrictOwner(target);
        } finally {
            if (!moved) {
                temp.delete();
            }
        }
    }

    static void atomicReplace(File temp, File target) throws IOException {
        try {
            Files.move(temp.toPath(), target.toPath(),
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static void restrictOwner(File target) {
        if (target == null) {
            return;
        }
        try {
            Set<PosixFilePermission> permissions = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(target.toPath(), permissions);
            return;
        } catch (Throwable ignored) {
        }
        try {
            target.setReadable(false, false);
            target.setWritable(false, false);
            target.setExecutable(false, false);
            target.setReadable(true, true);
            target.setWritable(true, true);
        } catch (Throwable ignored) {
        }
    }

    private static void writeBytesAtomic(File target, byte[] bytes, boolean privateFile) throws IOException {
        File parent = target.getParentFile();
        ensureDirectory(parent);
        File temp = File.createTempFile(target.getName() + ".", ".tmp", parent);
        boolean moved = false;
        try {
            if (privateFile) {
                restrictOwner(temp);
            }
            FileOutputStream out = new FileOutputStream(temp);
            try {
                out.write(bytes);
                out.getFD().sync();
            } finally {
                out.close();
            }
            if (privateFile) {
                restrictOwner(temp);
            }
            atomicReplace(temp, target);
            moved = true;
            if (privateFile) {
                restrictOwner(target);
            }
        } finally {
            if (!moved) {
                temp.delete();
            }
        }
    }

    private static String safeDisplay(String value) {
        if (value == null || value.length() == 0) {
            return "(empty)";
        }
        String clean = value.replace('\n', ' ').replace('\r', ' ');
        return clean.length() > 160 ? clean.substring(0, 160) + "..." : clean;
    }
}
