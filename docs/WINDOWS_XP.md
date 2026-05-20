# Windows XP Offline/Classic Guide

Windows XP is supported for classic/offline play.

Modern Microsoft login and fresh online downloads are best-effort on XP because XP-era TLS, root
certificates, Internet Explorer, and Java networking are the limiting pieces.

XP mode is intended for offline/classic play only.

## Recommended XP flow

Double-click:

```bat
Start MCLauncherRevival XP Offline.cmd
```

This starts the launcher with:

```text
-Dmclauncher.xpMode=true
```

XP mode keeps the classic launcher usable while avoiding fragile Microsoft login assumptions.

## Supported on XP

- Opening the launcher on an XP-compatible Java runtime.
- Offline/singleplayer launch mode.
- Launching old versions that are already downloaded into `.minecraft`.
- Profile editor shortcuts, saves backup, and texture pack import.

## Best-effort on XP

- Microsoft Login.
- Fresh Mojang/Minecraft version downloads.
- GitHub/Adoptium Java downloads.
- Modern HTTPS endpoints that require newer TLS/root certificates.

The launcher is built to avoid collecting raw Microsoft passwords, so it will not add old
password-based login as a workaround.

## Java runtime

The launcher jar is compiled as Java 7 bytecode.

Recommended:

- Java 7 on Windows XP, or
- an XP-compatible Java 8 build if you already have one.

If the release package does not include Java, follow the full manual guide:

- [XP Java setup](XP_JAVA_SETUP.md)

If a bundled-Java XP release is published, it may include a maintainer-provided runtime at:

```text
tools\java7\bin\java.exe
```

It may also include local installer EXEs under:

```text
tools\java-installers
```

The launcher does not choose, download, or silently install Java automatically. The maintainer must
manually place a verified redistributable Java 7 or XP-compatible Java 8 runtime at `tools\java7`,
or verified Java installer EXEs at `tools\java-installers`, before creating the XP bundled-Java
package. If Java is missing and installers are present, the XP script asks before running one.

Bundled Java is third-party software under its own license/readme files. Old Java runtimes are not
secure for general browsing or production use; use them only for this offline/classic launcher
scenario.

The normal `Start MCLauncherRevival.cmd` path can still be used on Windows 7 through Windows 11.

## Troubleshooting

### MCLauncherRevival.jar was not found

This usually means you downloaded GitHub's source-code ZIP or auto-generated tag/source ZIP instead
of the attached release package.

XP mode expects `MCLauncherRevival.jar` to already be included in the extracted release folder. XP
mode does not build from source automatically, and it does not try to download modern Java from
Adoptium by default.

Download the attached release ZIP from GitHub Releases, not the green **Code -> Download ZIP**
source archive and not the tag/source ZIP.

The correct release asset should be named like:

```text
MCLauncherRevival-v0.2.5-alpha.zip
```

If you only have the source archive, build `MCLauncherRevival.jar` on Windows 7 or newer, then copy
the jar into the XP launcher folder.

### Java runtime not found

If XP mode cannot find Java, use the XP bundled-Java release package if one is available, run a
bundled Java installer when prompted, or place a verified XP-compatible runtime at:

```text
tools\java7\bin\java.exe
```

XP mode checks `tools\java7`, `tools\jdk8`, `JAVA_HOME`, and `java.exe` on `PATH`, but
`tools\java7` is the preferred location for an already-extracted XP bundled-Java runtime.

Detailed download/install instructions are in [XP Java setup](XP_JAVA_SETUP.md).

### XP script says `choice` is not recognized

Some Windows XP installs do not include `choice.exe`. The XP offline launcher path is designed to
avoid `choice.exe` and should use plain command-prompt behavior instead.

If Java is missing, XP mode does not try to download Eclipse Temurin from Adoptium by default.
XP-era TLS, root certificates, and modern Java support are unreliable for that path.

Use one of these options instead:

- Install Java 7 on the XP machine.
- Install an XP-compatible Java 8 runtime if you already have one.
- Use the XP bundled-Java release package if one is available.
- Run one of the bundled Java installers if the package includes `tools\java-installers`.
- Manually extract a compatible runtime to `tools\java7`.
- Manually extract a compatible runtime to `tools\jdk8`.

### Release package should run the included jar

When using the GitHub Releases ZIP, XP offline/classic mode should run the included
`MCLauncherRevival.jar` and avoid rebuilding when possible.

Only source builds require `javac.exe` and `jar.exe`. Running the packaged jar only requires
`java.exe`.

If Java is not found, the script should print:

```text
Windows XP offline/classic mode needs Java 7 or an XP-compatible Java 8 runtime.
Use the XP bundled-Java release package, run a bundled Java installer, or place Java at tools\java7.
Expected runtime path: tools\java7\bin\java.exe
```

Modern Microsoft login and fresh HTTPS downloads are not reliable on XP. Use Offline Play with
versions that are already downloaded whenever possible.

## Download versions on another PC if needed

If XP cannot download version files because HTTPS fails, launch the desired version once on a newer
Windows PC first, then copy the relevant `.minecraft` folders to the XP machine:

```text
%APPDATA%\.minecraft\versions
%APPDATA%\.minecraft\libraries
%APPDATA%\.minecraft\assets
```

Then use `Play Offline` on XP.
