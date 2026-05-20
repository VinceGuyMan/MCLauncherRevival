# Alpha Release Guide

## Current status

Alpha releases are distributed as attached ZIP files on GitHub Releases.

Use the attached release ZIP, not GitHub's green **Code -> Download ZIP** source archive. The source
archive is useful for code review, but it is not the same as the runnable release package.

For `v0.2.0-alpha`, download:

```text
MCLauncherRevival-v0.2.0-alpha.zip
```

Extract it, then double-click:

```bat
Start MCLauncherRevival.cmd
```

For Windows XP offline/classic mode, double-click:

```bat
Start MCLauncherRevival XP Offline.cmd
```

## What the release should contain

Recommended release zip layout:

```text
MCLauncherRevival-v0.2.0-alpha/
  Start MCLauncherRevival.cmd
  Start MCLauncherRevival XP Offline.cmd
  run-win7.cmd
  build-win7.cmd
  MCLauncherRevival.jar
  resources/
  tools/
  docs/
  README.md
```

The jar can be built locally:

```bat
build-win7.cmd
```

## Notes

- Java 8 is recommended on Windows 7 and newer.
- Windows 7 dependency setup forces TLS 1.2 for the Temurin 8 download and will reuse
  `tools\jdk8` or `tools\temurin8-jdk.zip` if either is already present.
- Microsoft login opens in the user's default browser and may require the redirect URL paste
  fallback on Windows 7.
- Microsoft accounts need an Xbox profile before XSTS/Minecraft services login can succeed.
- Windows XP is supported for offline/classic play with Java 7 or an XP-compatible Java 8 build.
- The launcher never asks for a raw Microsoft password.
- If the jar is missing, `run-win7.cmd` will attempt to build it.

