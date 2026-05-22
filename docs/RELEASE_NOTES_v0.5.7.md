# MCLauncherRevival v0.5.7-alpha Release Notes

v0.5.7-alpha is a small release-prep polish update focused on the Windows command-window experience before the Swing launcher opens.

## Highlights

- Added a cleaner retro CMD startup screen with the large MCLauncherRevival ASCII banner.
- Added separate Windows 7+ and XP offline/classic boot cards.
- Removed UTF-8 BOM batch-file issues that could make CMD print raw `@echo off` errors before startup.
- Kept normal startup quiet and moved the visual startup flow into `scripts\run-win7.cmd`.
- Added `MCLR_BANNER_SHOWN` so nested launcher/build calls do not duplicate the banner.
- Simplified normal status output to short `[ OK ]`, `[WARN]`, `[INFO]`, `[ XP ]`, and `[FAIL]` messages.
- Startup now ends with `Press Nothing. Happy Mining!` before the launcher window opens.
- Added `--verbose` and `MCLR_VERBOSE=1` troubleshooting support.
- Improved Windows Java detection for nested portable JDK folders, including layouts such as `tools\jdk8\jdk8u492-b09`.
- If Java is missing on Windows 7+, pressing Enter at the prompt now defaults to downloading the portable Java 8 JDK into `tools\jdk8`.

## Included package

Download the attached release asset:

```text
MCLauncherRevival-v0.5.7-alpha.zip
```

Use the attached release ZIP, not GitHub's green Code -> Download ZIP source archive, for normal use.

## Notes

- This project is unofficial alpha software.
- It is not affiliated with, endorsed by, or sponsored by Mojang, Microsoft, Xbox, or Minecraft.
- The launcher should never ask for your raw Microsoft password inside the app.
- Windows XP remains offline/classic focused, and modern login/download behavior is best-effort only.
