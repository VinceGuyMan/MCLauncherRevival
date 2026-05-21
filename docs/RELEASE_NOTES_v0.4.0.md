# MCLauncherRevival v0.4.0-alpha Release Notes

This alpha release is a documentation and release-polish pass built around real Windows XP field
testing.

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
MCLauncherRevival-v0.4.0-alpha.zip
```

Do not use GitHub's green **Code -> Download ZIP** source archive or auto-generated tag/source ZIP
as the runnable package. Those archives are useful for reading or building the code, but they may
not include `MCLauncherRevival.jar`.

## Windows XP field-test proof

Real Windows XP laptop testing now has screenshots in the repo showing:

- The XP test machine specs.
- Prepared `.minecraft` folders being copied from a newer PC to removable media.
- Prepared folders copied from removable media onto Windows XP.
- Minecraft Beta 1.7.3 reaching the main menu on Windows XP.
- Minecraft Beta 1.7.3 running in-game with the debug/FPS overlay.

Observed performance on the test laptop:

- Beta 1.7.3: around 15 FPS on lowest settings.
- Beta 1.4: around 2-5 FPS.
- Alpha 1.2.6: around 5-10 FPS.
- Alpha 1.2_01: around 12-20 FPS.

This confirms that XP Offline mode can work on real hardware, but performance depends heavily on
hardware, graphics drivers, Java version, and selected Minecraft version.

## Reminder: XP remains offline/classic only

Modern Microsoft login remains unsupported/best-effort on XP. Fresh HTTPS downloads may fail on XP.

The preferred XP workflow remains:

1. Prepare/download the desired version on Windows 7 or newer.
2. Copy `.minecraft\versions`, `.minecraft\libraries`, and `.minecraft\assets` to the XP machine.
3. Run `Start MCLauncherRevival XP Offline.cmd`.

Use only Minecraft files you own or otherwise have the right to use.

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
