package net.minecraft;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class MacFirstThreadMinecraft {
    private static final int DEFAULT_WIDTH = 854;
    private static final int DEFAULT_HEIGHT = 480;

    private MacFirstThreadMinecraft() {
    }

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName("Minecraft main thread");
        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        } catch (Throwable ignored) {
        }
        System.setProperty("apple.awt.application.name", "Minecraft");

        String version = arg(args, 0, env("MCLR_GAME_VERSION", "Minecraft"));
        int width = parseInt(arg(args, 1, String.valueOf(DEFAULT_WIDTH)), DEFAULT_WIDTH);
        int height = parseInt(arg(args, 2, String.valueOf(DEFAULT_HEIGHT)), DEFAULT_HEIGHT);
        String username = env("MCLR_GAME_USERNAME", "Player");
        String session = env("MCLR_GAME_SESSION", "-");
        File gameDir = new File(env("MCLR_GAME_DIR", defaultMinecraftDir().getAbsolutePath()));

        System.out.println("MCLauncherRevival macOS wrapper: starting " + version);
        System.out.println("MCLauncherRevival macOS wrapper: gameDir=" + gameDir.getAbsolutePath());

        Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
        setStaticMinecraftDir(minecraftClass, gameDir);

        Class<?> implementationClass = minecraftImplementation(minecraftClass);
        Frame frame = null;
        Object minecraft;
        try {
            frame = new Frame("Minecraft");
            Canvas canvas = new Canvas();
            frame.setLayout(new BorderLayout());
            frame.add(canvas, BorderLayout.CENTER);
            canvas.setPreferredSize(new Dimension(width, height));
            frame.pack();
            frame.setLocationRelativeTo((Component) null);
            minecraft = newMinecraft(implementationClass, frame, canvas, width, height);
            System.out.println("MCLauncherRevival macOS wrapper: created AWT game window");
        } catch (Throwable frameFailure) {
            dispose(frame);
            frame = null;
            System.out.println("MCLauncherRevival macOS wrapper: AWT game window unavailable; using standalone LWJGL display ("
                    + simpleError(frameFailure) + ")");
            minecraft = newMinecraft(implementationClass, null, null, width, height);
        }
        setSession(minecraftClass, minecraft, username, session);
        if (frame != null) {
            installCloseHandler(frame, minecraft);
            frame.setVisible(true);
        }
        requestForeground(frame);
        System.out.println("MCLauncherRevival macOS wrapper: entering game run loop");

        try {
            ((Runnable) minecraft).run();
        } finally {
            System.out.println("MCLauncherRevival macOS wrapper: game run loop ended");
            System.exit(0);
        }
    }

    private static Class<?> minecraftImplementation(Class<?> minecraftClass) throws Exception {
        if (!Modifier.isAbstract(minecraftClass.getModifiers())) {
            return minecraftClass;
        }
        String classPath = System.getProperty("java.class.path", "");
        StringTokenizer tokenizer = new StringTokenizer(classPath, File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            File file = new File(tokenizer.nextToken());
            if (!file.isFile() || !file.getName().endsWith(".jar")) {
                continue;
            }
            Class<?> implementationClass = findMinecraftImplementationInJar(minecraftClass, file);
            if (implementationClass != null) {
                return implementationClass;
            }
        }
        throw new IllegalStateException("Could not find concrete Minecraft implementation class.");
    }

    private static Class<?> findMinecraftImplementationInJar(Class<?> minecraftClass, File jarFile) {
        JarFile jar = null;
        try {
            jar = new JarFile(jarFile);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() || !name.endsWith(".class") || name.indexOf('$') >= 0) {
                    continue;
                }
                try {
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    Class<?> candidate = Class.forName(className, false, minecraftClass.getClassLoader());
                    int modifiers = candidate.getModifiers();
                    if (minecraftClass.isAssignableFrom(candidate)
                            && !Modifier.isAbstract(modifiers)
                            && minecraftConstructor(candidate) != null) {
                        return candidate;
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
            return null;
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    private static Object newMinecraft(Class<?> implementationClass, Component parent, Canvas canvas, int width, int height)
            throws Exception {
        Constructor<?> constructor = minecraftConstructor(implementationClass);
        constructor.setAccessible(true);
        if (constructor.getParameterTypes().length == 7) {
            return constructor.newInstance(parent, canvas, null, width, height, false, parent);
        }
        return constructor.newInstance(parent, canvas, null, width, height, false);
    }

    private static Constructor<?> minecraftConstructor(Class<?> minecraftClass)
            throws NoSuchMethodException {
        Constructor<?>[] constructors = minecraftClass.getDeclaredConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor<?> constructor = constructors[i];
            Class<?>[] types = constructor.getParameterTypes();
            if ((types.length == 6 || types.length == 7)
                    && "java.awt.Component".equals(types[0].getName())
                    && "java.awt.Canvas".equals(types[1].getName())
                    && "net.minecraft.client.MinecraftApplet".equals(types[2].getName())
                    && Integer.TYPE.equals(types[3])
                    && Integer.TYPE.equals(types[4])
                    && Boolean.TYPE.equals(types[5])
                    && (types.length == 6 || "java.awt.Frame".equals(types[6].getName()))) {
                return constructor;
            }
        }
        throw new NoSuchMethodException(minecraftClass.getName());
    }

    private static void setStaticMinecraftDir(Class<?> minecraftClass, File gameDir) throws Exception {
        Field[] fields = minecraftClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && File.class.equals(field.getType())) {
                field.setAccessible(true);
                field.set(null, gameDir);
                return;
            }
        }
        throw new IllegalStateException("Could not find Minecraft game directory field.");
    }

    private static void setSession(Class<?> minecraftClass, Object minecraft, String username, String session)
            throws Exception {
        Field[] fields = minecraftClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Constructor<?> constructor = twoStringConstructor(field.getType());
            if (constructor == null) {
                continue;
            }
            field.setAccessible(true);
            field.set(minecraft, constructor.newInstance(username, session));
            return;
        }
        throw new IllegalStateException("Could not find Minecraft session field.");
    }

    private static Constructor<?> twoStringConstructor(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor(String.class, String.class);
            constructor.setAccessible(true);
            return constructor;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void installCloseHandler(Frame frame, Object minecraft) {
        final Object game = minecraft;
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                if (!requestShutdown(game)) {
                    System.exit(0);
                }
            }
        });
    }

    private static boolean requestShutdown(Object minecraft) {
        if (minecraft == null) {
            return false;
        }
        String[] methodNames = {"f", "shutdownMinecraftApplet", "stop"};
        for (int i = 0; i < methodNames.length; i++) {
            try {
                Method method = minecraft.getClass().getMethod(methodNames[i]);
                if (method.getParameterTypes().length == 0 && Void.TYPE.equals(method.getReturnType())) {
                    method.invoke(minecraft);
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    private static void requestForeground(Frame frame) {
        if (frame != null) {
            try {
                frame.setState(Frame.NORMAL);
                frame.toFront();
                frame.requestFocus();
            } catch (Throwable ignored) {
            }
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

    private static void dispose(Frame frame) {
        if (frame != null) {
            try {
                frame.dispose();
            } catch (Throwable ignored) {
            }
        }
    }

    private static String simpleError(Throwable failure) {
        if (failure == null) {
            return "unknown error";
        }
        String message = failure.getMessage();
        return failure.getClass().getSimpleName() + (message == null || message.length() == 0 ? "" : ": " + message);
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

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static File defaultMinecraftDir() {
        return new File(new File(System.getProperty("user.home"), "Library/Application Support"), "minecraft");
    }
}
