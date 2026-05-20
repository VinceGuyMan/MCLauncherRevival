# MCLauncherRevive

A revived classic Minecraft Java launcher inspired by the February 2011 launcher UI, updated with modern Microsoft authentication and old-version launching.

The goal is intentionally weird and cozy: keep the dirt-texture news panel, tiny grey controls, old launcher proportions, and nostalgic update-feed feel, while replacing the unsafe legacy Mojang username/password login with a modern authentication chain.

## Features

- Classic 2011-style launcher window and news/sidebar layout.
- Microsoft browser OAuth login. No raw Microsoft passwords are requested or stored.
- Modern auth chain:
  - Microsoft OAuth
  - Xbox Live authentication
  - XSTS authorization
  - Minecraft services login
  - Minecraft profile lookup
- Offline singleplayer fallback.
- Selectable classic versions from Beta 1.8.x down through old Alpha/Classic versions.
- Version download status and redownload control.
- Java 8 compatibility warnings.
- Local profile/settings page with useful folder shortcuts.
- Save backup helper.
- Texture pack `.zip` import helper.
- Lightweight Java 8-compatible source, no external build system required.

## Quick start on Windows

Double-click:

```bat
run-win7.cmd
```

If Java is missing, the script offers to download a portable Eclipse Temurin 8 JDK into:

```text
tools\jdk8
```

That Java download is local to this folder and is not installed system-wide.

## Build on Windows

Double-click or run:

```bat
build-win7.cmd
```

The build creates:

```text
MCLauncherRevive-modern.jar
```

Run it directly with:

```bat
java -jar MCLauncherRevive-modern.jar
```

## Supported operating systems

Primary target:

- Windows 7 SP1 through Windows 11
- Java 8 recommended

Experimental/manual:

- Linux
- macOS

The `.cmd` helper scripts are Windows-specific. The Java source itself is intentionally lightweight, but old Minecraft Beta/Alpha LWJGL natives can be picky outside Windows.

## Where data is stored

Minecraft files use the normal `.minecraft` folder:

```text
%APPDATA%\.minecraft
```

Launcher-specific files:

```text
%APPDATA%\.minecraft\launcher_revive
```

Token cache:

```text
%APPDATA%\.minecraft\launcher_revive\auth.properties
```

The launcher caches OAuth tokens, profile name, and UUID. It does not store Microsoft passwords. Use `Forget Login` in the launcher to remove cached tokens.

## GitHub release notes

For source control, commit the source, scripts, docs, and lightweight bundled artwork needed by the launcher. Do not commit downloaded Java runtimes, build output folders, or local caches.

For a GitHub Release, attach the generated `MCLauncherRevive-modern.jar` as a release asset after building it locally or from CI.

See:

- [Windows 7 guide](docs/WINDOWS_7.md)
- [Release checklist](docs/RELEASE_CHECKLIST.md)
- [Modernization notes](MODERNIZATION.md)
- [Security notes](SECURITY.md)

## Disclaimer

This is an unofficial nostalgia project. It is not affiliated with Mojang, Microsoft, Xbox, Minecraft, Scrolls/Caller's Bane, or Cobalt.

Minecraft names, artwork, versions, and services belong to their respective owners. This project downloads Minecraft client/version metadata from official Mojang/Minecraft endpoints for users who already have the right to use Minecraft.

Vibe-Coded with Codex.
