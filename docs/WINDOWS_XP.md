# Windows XP Offline/Classic Guide

Windows XP is supported for classic/offline play.

Modern Microsoft login and fresh online downloads are best-effort on XP because XP-era TLS, root
certificates, Internet Explorer, and Java networking are the limiting pieces.

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

The normal `Start MCLauncherRevival.cmd` path can still be used on Windows 7 through Windows 11.

## Troubleshooting

### XP script says `choice` is not recognized

Some Windows XP installs do not include `choice.exe`. The XP offline launcher path is designed to
avoid `choice.exe` and should use plain command-prompt behavior instead.

If Java is missing, XP mode does not try to download Eclipse Temurin from Adoptium by default.
XP-era TLS, root certificates, and modern Java support are unreliable for that path.

Use one of these options instead:

- Install Java 7 on the XP machine.
- Install an XP-compatible Java 8 runtime if you already have one.
- Manually extract a compatible runtime to `tools\jdk8`.

### Release package should run the included jar

When using the GitHub Releases ZIP, XP offline/classic mode should run the included
`MCLauncherRevival.jar` and avoid rebuilding when possible.

Only source builds require `javac.exe` and `jar.exe`. Running the packaged jar only requires
`java.exe`.

If Java is not found, the script should print:

```text
Windows XP offline/classic mode needs Java 7 or an XP-compatible Java 8 runtime already installed or extracted at tools\jdk8.
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
