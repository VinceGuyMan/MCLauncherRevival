# MCLauncherRevival v0.4.5-alpha Release Notes

This alpha release is a platform audit and release-confidence pass. It keeps the nostalgic launcher
UI intact while tightening scripts, documentation, and safety checks around Windows XP, Windows 7+,
macOS, and Linux behavior.

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
MCLauncherRevival-v0.4.5-alpha.zip
```

Do not use GitHub's green **Code -> Download ZIP** source archive or auto-generated tag/source ZIP
as the runnable package. Those archives are useful for reading or building the code, but they may
not include `MCLauncherRevival.jar`.

## Highlights

- Added `Setup MCLauncherRevival.cmd` as the recommended Windows first-run hub.
- Hardened `Redownload Version` so it can only delete the selected folder directly under
  `.minecraft\versions`.
- Windows scripts now preserve Java launcher failure exit codes for better setup/package checks.
- Microsoft browser login now uses authorization-code login with `offline_access`, allowing refresh
  token caching when Microsoft returns a refresh token.
- The no-Xbox-profile XSTS error now points users to `https://start.ui.xboxlive.com/`, then
  `Forget Login`, then retry.
- Added best-effort macOS/Linux browser fallbacks after Java desktop browsing.
- Made OpenGL/LWJGL failure diagnostics platform-neutral instead of XP-only.
- Expanded Windows 7+, XP, macOS, Linux, auth-flow, and release checklist docs.

## Windows XP

XP remains offline/classic only. Real XP hardware testing has shown old Minecraft can run when the
right Java runtime, version files, and graphics drivers are present, but performance is strongly
hardware-dependent.

Use `Setup MCLauncherRevival.cmd`, choose XP Offline / Classic mode, and prepare Minecraft files on
Windows 7 or newer if XP cannot download them.

## Windows 7 and newer

Windows 7-11 remain the primary full launcher target. Java 8 is recommended. Windows 7 users may
still need the redirect paste fallback if Microsoft changes the browser redirect URL to
`removed=true`.

## macOS and Linux

macOS and Linux remain experimental. The launcher UI may open, but old Beta/Alpha clients can fail
with blank windows, OpenGL errors, or LWJGL native-library issues.

Logs:

```text
macOS: ~/Library/Application Support/minecraft/launcher_revive/logs/last-launch.log
Linux: ~/.minecraft/launcher_revive/logs/last-launch.log
```

## Account safety

The launcher should never ask for a raw Microsoft password inside the app. Microsoft sign-in should
use browser/OAuth flow where available.

Offline Play remains available when online authentication fails.

## Run

After extracting the release ZIP on Windows, run:

```bat
Setup MCLauncherRevival.cmd
```

For direct Windows 7-11 startup:

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
