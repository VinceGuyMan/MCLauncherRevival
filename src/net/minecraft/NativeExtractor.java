package net.minecraft;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class NativeExtractor {
    private static final long MAX_ENTRY_BYTES = 64L * 1024L * 1024L;
    private static final long MAX_TOTAL_BYTES = 256L * 1024L * 1024L;

    private NativeExtractor() {
    }

    static void extractFlat(File nativeJar, File nativeDir) throws IOException {
        SafeFiles.ensureDirectory(nativeDir);
        ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(nativeJar)));
        long total = 0L;
        try {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = in.getNextEntry()) != null) {
                try {
                    String name = entry.getName();
                    if (entry.isDirectory() || unsafeEntry(name)) {
                        continue;
                    }
                    String baseName = new File(name.replace('\\', '/')).getName();
                    if (baseName.length() == 0 || ".".equals(baseName) || "..".equals(baseName)) {
                        continue;
                    }
                    File outFile = SafeFiles.resolveInside(nativeDir, baseName, "native-library destination");
                    File temp = File.createTempFile("mclr-native-", ".tmp", nativeDir);
                    boolean moved = false;
                    long entryBytes = 0L;
                    try {
                        FileOutputStream out = new FileOutputStream(temp);
                        try {
                            int read;
                            while ((read = in.read(buffer)) != -1) {
                                entryBytes += read;
                                total += read;
                                if (entryBytes > MAX_ENTRY_BYTES || total > MAX_TOTAL_BYTES) {
                                    throw new IOException("Native archive exceeded the allowed extraction size.");
                                }
                                out.write(buffer, 0, read);
                            }
                            out.getFD().sync();
                        } finally {
                            out.close();
                        }
                        SafeFiles.atomicReplace(temp, outFile);
                        moved = true;
                    } finally {
                        if (!moved) {
                            temp.delete();
                        }
                    }
                } finally {
                    in.closeEntry();
                }
            }
        } finally {
            in.close();
        }
    }

    private static boolean unsafeEntry(String name) {
        if (name == null || name.length() == 0 || name.indexOf('\0') >= 0) {
            return true;
        }
        String normalized = name.replace('\\', '/');
        String upper = normalized.toUpperCase(Locale.ENGLISH);
        if (normalized.startsWith("/") || upper.startsWith("META-INF/")) {
            return true;
        }
        String[] parts = normalized.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("..".equals(parts[i])) {
                return true;
            }
        }
        return false;
    }
}
