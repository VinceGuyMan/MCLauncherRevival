# MCLauncherRevival v0.2.5 Release Notes

This alpha release focuses on compatibility cleanup after real Windows XP testing, plus preliminary
Linux and macOS helper scripts.

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
MCLauncherRevival-v0.2.5-alpha.zip
```

Do not use GitHub's green **Code -> Download ZIP** source archive as the runnable package. The source
archive is useful for reading the code, but it is not the same as the packaged launcher release.

## Windows XP offline/classic startup fix

The XP offline launcher path was updated after testing on a real Windows XP laptop.

Fixes and behavior changes:

- XP mode no longer depends on `choice.exe`, which is missing on some XP installs.
- XP mode does not try to auto-download modern Eclipse Temurin from Adoptium by default.
- If `MCLauncherRevival.jar` already exists, XP mode runs the jar with `java.exe` only.
- `javac.exe` and `jar.exe` are only required when building from source.
- XP mode prints clearer startup messages, including:
  - `XP offline mode detected.`
  - `Online login and fresh downloads are disabled/best-effort on XP.`
  - `Using existing MCLauncherRevival.jar.`
  - `Java runtime found: <path>`
  - `Java runtime not found.`

If Java is missing on XP, install Java 7 or an XP-compatible Java 8 runtime, or manually extract it
to:

```text
tools\jdk8
```

## Preliminary Linux support

Added:

- `run-linux.sh`
- `build-linux.sh`
- `docs/LINUX.md`

These scripts passed shell syntax checks and build-wrapper checks under Git Bash, but this is not yet
a confirmed native Linux distro field test.

## Preliminary macOS support

Added:

- `run-macos.sh`
- `build-macos.sh`
- `docs/MACOS.md`

These scripts passed shell syntax checks and build-wrapper checks under Git Bash, but this is not yet
a confirmed native macOS field test. There is no signed or notarized `.app` bundle yet.

## Existing Windows 7 notes

Windows 7 dependency setup still forces TLS 1.2 for the Temurin 8 download path and can reuse
`tools\jdk8` or `tools\temurin8-jdk.zip` if present.

Microsoft login opens in the user's default browser. On older systems, users may need the redirect
URL paste fallback.

## Account safety

The launcher should never ask for a raw Microsoft password inside the app. Microsoft sign-in should
use browser/OAuth flow where available.

Offline Play remains available when online authentication fails.

## Run

After extracting the release ZIP on Windows, run:

```bat
Start MCLR.cmd
```

For Windows XP offline/classic mode:

```bat
Start MCLR XP.cmd
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
