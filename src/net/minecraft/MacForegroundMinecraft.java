package net.minecraft;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class MacForegroundMinecraft {
    private MacForegroundMinecraft() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("apple.awt.application.name", "Minecraft");
        System.setProperty("apple.awt.UIElement", "false");
        System.setProperty("java.awt.headless", "false");

        String version = arg(args, 0, env("MCLR_GAME_VERSION", "Minecraft"));
        String username = env("MCLR_GAME_USERNAME", "Player");
        String session = env("MCLR_GAME_SESSION", "-");
        File gameDir = new File(env("MCLR_GAME_DIR", defaultMinecraftDir().getAbsolutePath()));

        System.out.println("MCLauncherRevival macOS foreground wrapper: starting " + version);
        System.out.println("MCLauncherRevival macOS foreground wrapper: gameDir=" + gameDir.getAbsolutePath());

        Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
        setStaticMinecraftDirIfPresent(minecraftClass, gameDir);
        Method main = minecraftClass.getMethod("main", new Class[] {String[].class});
        main.invoke(null, new Object[] {new String[] {username, session}});
        requestForeground();
    }

    private static void setStaticMinecraftDirIfPresent(Class<?> minecraftClass, File gameDir) {
        Field[] fields = minecraftClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && File.class.equals(field.getType())) {
                try {
                    field.setAccessible(true);
                    field.set(null, gameDir);
                } catch (Throwable ignored) {
                }
                return;
            }
        }
    }

    private static void requestForeground() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {
            Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
            Method getApplication = applicationClass.getMethod("getApplication");
            Object application = getApplication.invoke(null);
            Method requestForeground = applicationClass.getMethod("requestForeground", boolean.class);
            requestForeground.invoke(application, Boolean.TRUE);
        } catch (Throwable ignored) {
        }
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.length() == 0 ? fallback : value;
    }

    private static String arg(String[] args, int index, String fallback) {
        return args != null && args.length > index && args[index] != null && args[index].length() > 0
                ? args[index]
                : fallback;
    }

    private static File defaultMinecraftDir() {
        return new File(new File(System.getProperty("user.home"), "Library/Application Support"), "minecraft");
    }
}
