# MCLauncherRevival v0.3.0-alpha Release Notes

This alpha release focuses on safer release packaging and clearer Windows XP Java setup paths.

## Status

This is still alpha / experimental software.

- Unofficial project.
- Not affiliated with Mojang, Microsoft, Xbox, or Minecraft.
- Not a replacement for the official Minecraft Launcher.
- Intended for preservation, nostalgia, and learning.
- Use at your own risk.

## Download the release ZIP

Use the attached GitHub Releases ZIP:

```text
MCLauncherRevival-v0.3.0-alpha.zip
```

Do not use GitHub's green **Code -> Download ZIP** source archive or auto-generated tag/source ZIP
as the runnable package. Those archives are useful for reading or building the code, but they may
not include `MCLauncherRevival.jar`.

## Release packaging cleanup

- Added release-package guidance so users do not accidentally download GitHub source/tag ZIPs.
- Added `package-release.cmd` to build, stage, zip, list, and verify normal release packages.
- The package step fails if `MCLauncherRevival.jar` is missing.
- The package step prints final ZIP contents and verifies that `MCLauncherRevival.jar` is included.

## Windows XP Java setup

XP remains offline/classic only. Modern Microsoft login and fresh HTTPS downloads are best-effort on
XP because of old TLS, certificate, browser, and Java limitations.

New XP Java support:

- XP mode prefers `tools\java7\bin\java.exe` when present.
- XP mode still checks `tools\jdk8`, `JAVA_HOME`, and `java.exe` on `PATH`.
- If Java is missing and `tools\java-installers` exists, the launcher asks before running one of the
  bundled installers.
- Added [XP Java setup](XP_JAVA_SETUP.md) for releases that cannot redistribute Java publicly.
- Added `package-xp-release.cmd` for maintainer-created XP bundled-Java packages.

Bundled Java, when present, is third-party software under its own license/readme files. Old Java
runtimes are not secure for general browsing, browser plugins, or production use.

## Account safety

The launcher should never ask for a raw Microsoft password inside the app. Microsoft sign-in should
use browser/OAuth flow where available.

Offline Play remains available when online authentication fails.

## Run

After extracting the release ZIP on Windows, run:

```bat
Start MCLauncherRevival.cmd
```

For Windows XP offline/classic mode:

```bat
Start MCLauncherRevival XP Offline.cmd
```

For preliminary Linux testing:

```sh
chmod +x run-linux.sh build-linux.sh
./run-linux.sh
```

For preliminary macOS testing:

```sh
chmod +x run-macos.sh build-macos.sh
./run-macos.sh
```

## Disclaimer

Minecraft is a trademark of Mojang/Microsoft. This project is unofficial and is not affiliated with,
endorsed by, or sponsored by Mojang, Microsoft, Xbox, or Minecraft.
