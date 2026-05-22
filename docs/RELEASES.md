# Alpha Release Guide

## Current status

Alpha releases are distributed as attached ZIP files on GitHub Releases.

Use the attached release ZIP asset from GitHub Releases.

Do not use these files for normal play:

- GitHub's green **Code -> Download ZIP** button.
- GitHub's auto-generated tag/source ZIP files.

Those source archives are useful for reading or building the code, but they may not include
`MCLauncherRevival.jar`. The attached release asset is the runnable package.

For `v0.5.6`, download:

```text
MCLauncherRevival-v0.5.6-alpha.zip
```

If an XP bundled-Java package is published, it should be named:

```text
MCLauncherRevival-v0.5.6-alpha-xp-bundled-java.zip
```

That package is only for XP offline/classic use. It may include a maintainer-provided Java runtime
from `tools\java7`, or local installer EXEs from `tools\java-installers`. The project does not
choose, download, or fetch Java from third-party mirrors during packaging.

If Java cannot be redistributed in a public release asset, publish the normal release ZIP and point
XP users to [XP Java setup](XP_JAVA_SETUP.md).

Extract it, then double-click:

```bat
Setup MCLauncherRevival.cmd
```

The setup hub can auto-detect or ask which Windows path to use, then route to the correct script.

For direct Windows 7-11 startup, double-click:

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
MCLauncherRevival-v0.5.6-alpha/
  Setup MCLauncherRevival.cmd
  Start MCLauncherRevival.cmd
  Start MCLauncherRevival XP Offline.cmd
  MCLauncherRevival.jar
  scripts/
    run-win7.cmd
    build-win7.cmd
    run-linux.sh
    build-linux.sh
    run-macos.sh
    build-macos.sh
  resources/
    net/minecraft/themes/        recreated era layout textures
  tools/
  docs/
  README.md
  CHANGELOG.md
  LICENSE
  NOTICE.md
```

Recommended XP bundled-Java release zip layout:

```text
MCLauncherRevival-v0.5.6-alpha-xp-bundled-java/
  Setup MCLauncherRevival.cmd
  Start MCLauncherRevival XP Offline.cmd
  Start MCLauncherRevival.cmd
  MCLauncherRevival.jar
  scripts/
    run-win7.cmd
  resources/
    net/minecraft/themes/        recreated era layout textures
  tools/
    java7/                    optional extracted runtime
      bin/
        java.exe
      license/readme files from the Java distributor
    java-installers/          optional local installer EXEs
  docs/
  README.md
  LICENSE
  NOTICE.md
```

The jar can be built locally:

```bat
scripts\build-win7.cmd
```

Version-specific notes for this release are in
[RELEASE_NOTES_v0.5.6.md](RELEASE_NOTES_v0.5.6.md).

## Notes

- Java 8 is recommended on Windows 7 and newer.
- Windows 7 dependency setup forces TLS 1.2 for the Temurin 8 download and will reuse
  `tools\jdk8` or `tools\temurin8-jdk.zip` if either is already present.
- Microsoft login opens in the user's default browser and may require the redirect URL paste
  fallback on Windows 7.
- Microsoft accounts need an Xbox profile before XSTS/Minecraft services login can succeed.
- Windows XP is supported for offline/classic play with Java 7 or an XP-compatible Java 8 build.
- XP bundled-Java packages must be created with `scripts\package-xp-release.cmd` after manually
  placing a verified redistributable runtime at `tools\java7` or verified installer EXEs at
  `tools\java-installers`.
- If Java is missing and `tools\java-installers` exists, the XP launcher asks before running a
  bundled installer.
- If Java installers cannot be included publicly, use the normal release package and document
  [XP Java setup](XP_JAVA_SETUP.md) instead.
- Bundled Java is third-party software under its own license/readme files. Old Java runtimes are not
  secure for general browsing or production use.
- Linux shell wrappers are included for preliminary testing, but old Minecraft/LWJGL game launch is
  experimental. Linux logs are written to `~/.minecraft/launcher_revive/logs/last-launch.log`.
- macOS testing can open the launcher, but old client launch may hang on a blank Minecraft window
  due to LWJGL/OpenGL/native compatibility. macOS logs are written to
  `~/Library/Application Support/minecraft/launcher_revive/logs/last-launch.log`.
- The launcher never asks for a raw Microsoft password.
- v0.5.0 adds recreated historical launcher style layouts for Beta, Alpha, Infdev, Classic, and
  Pre-Classic modes. These are inspired by historical launcher references but use project-owned
  recreated assets instead of redistributed proprietary launcher files.
- If the jar is missing on Windows 7 or newer, `scripts\run-win7.cmd` will attempt to build it.
- If the jar is missing in XP offline mode, download the attached release ZIP instead of the
  source-code or tag ZIP.


