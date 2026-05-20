# Alpha Release Guide

## Current status

No public release has been published yet. Build from source for now.

When an alpha release is available, download the release zip from GitHub Releases, extract it, then
double-click:

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
MCLauncherRevival/
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
- Windows XP is supported for offline/classic play with Java 7 or an XP-compatible Java 8 build.
- The launcher never asks for a raw Microsoft password.
- If the jar is missing, `run-win7.cmd` will attempt to build it.

