# MCLauncherRevival v0.3.5-alpha Release Notes

This alpha release focuses on field-tested Windows XP offline/classic behavior, clearer old-version
setup guidance, and more honest macOS launch diagnostics.

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
MCLauncherRevival-v0.3.5-alpha.zip
```

Do not use GitHub's green **Code -> Download ZIP** source archive or auto-generated tag/source ZIP
as the runnable package. Those archives are useful for reading or building the code, but they may
not include `MCLauncherRevival.jar`.

## Windows XP field test

Real Windows XP laptop testing confirmed that XP Offline mode can launch and run classic Minecraft
when the correct pieces are in place.

Observed test result:

- Launcher starts in XP Offline mode.
- Classic/offline Minecraft can launch.
- One tested XP laptop ran Beta 1.7.3 at around 15 FPS on the lowest settings.

XP remains supported for offline/classic play only. Performance depends heavily on hardware,
graphics drivers, Java version, and selected Minecraft version.

Recommended XP settings:

- Graphics: Fast
- Render Distance: Tiny or Short
- Smooth Lighting: Off
- Particles: Minimal
- Lower resolution if needed
- Limit background programs

## XP local version discovery

The launcher now scans `.minecraft\versions` on startup and adds locally prepared versions to the
version dropdown.

To appear in the dropdown, a version needs the Mojang launcher-style layout:

```text
.minecraft\versions\<version>\<version>.jar
.minecraft\versions\<version>\<version>.json
```

Loose jars such as `.minecraft\versions\b1.7.3.jar` are not enough. The launcher now explains the
required folder layout when it detects a loose jar.

## XP version preparation

The preferred method remains:

1. Prepare/download the desired version on Windows 7 or newer.
2. Copy `.minecraft\versions`, `.minecraft\libraries`, and `.minecraft\assets` to the XP machine.
3. Use `Start MCLR XP.cmd`.

MCVersions.net is documented only as an optional convenience for locating old client jars served
from Mojang. It does not replace the required JSON metadata, libraries, natives, and assets.

Use only Minecraft files you own or otherwise have the right to use.

## XP OpenGL troubleshooting

If Minecraft starts but fails with `Pixel format not accelerated` or no accelerated OpenGL mode,
that usually means the XP graphics driver does not expose hardware-accelerated OpenGL.

This is a graphics driver/OpenGL issue, not a Microsoft login or version-file issue. Install the
correct XP or XP x64 GPU driver for the machine, avoid generic Microsoft display drivers, and check
`dxdiag`.

## macOS compatibility warning

macOS can open the launcher UI in testing, but old Beta/Alpha Minecraft client launch is still
experimental. Some versions may open a blank Minecraft window, fail to render, or hang because of
old LWJGL/OpenGL/Java native compatibility.

The launcher warns before launching Minecraft on macOS and points users to:

```text
~/Library/Application Support/minecraft/launcher_revive/logs/last-launch.log
```

Windows remains the primary supported target.

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
