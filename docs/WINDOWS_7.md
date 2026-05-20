# Windows 7 Build and Run Guide

MCLauncherRevive supports Windows XP for offline/classic play and Windows 7 SP1 through modern Windows for the full modern-auth launcher experience. This guide covers Windows 7 and newer, where Java 8 is recommended. The jar is compiled as Java 7 bytecode so XP offline/classic mode remains supported.

## Run

Double-click:

```bat
run-win7.cmd
```

If Java is missing, the script offers to download Eclipse Temurin 8 JDK into:

```text
tools\jdk8
```

This is portable and local to the project folder.

## Build

Double-click:

```bat
build-win7.cmd
```

The build output is:

```text
MCLauncherRevive-modern.jar
```

## Manual Java install

If the automatic download fails, install a Java 8 JDK manually and make sure these are on `PATH`:

```text
java.exe
javac.exe
jar.exe
```

Then run:

```bat
build-win7.cmd
run-win7.cmd
```

## Common notes

- Java 8 is recommended for old Beta/Alpha Minecraft.
- Newer Java may run the launcher but old Minecraft/LWJGL can be picky.
- The launcher writes game files to the normal `%APPDATA%\.minecraft` folder.
- Offline mode remains available even if Microsoft login fails.
