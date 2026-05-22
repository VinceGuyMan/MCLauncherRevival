# MCLauncherRevival v0.5.8-alpha Release Notes

v0.5.8-alpha is a small trust, XP, and packaging cleanup release.

## Microsoft login trust flow

- Microsoft Login now starts with a clearer trust dialog before opening the browser.
- The primary login path uses the user's default browser with a temporary local callback and PKCE.
- Device-code login is kept as a fallback when the normal browser callback cannot complete.
- Manual paste-back is now advanced-only instead of the normal login path.
- The launcher messaging is clearer that Microsoft passwords stay in the browser on Microsoft's
  website, and MCLauncherRevival only receives returned OAuth tokens after sign-in approval.

## XP and low-end machines

- Low-End mode is more aggressive and now uses the `Potato 256MB` memory preset.
- XP/local version scanning is more forgiving for copied version folders.
- The launcher can now detect a local version folder containing exactly one `.jar` and exactly one
  `.json`, even if the filenames do not match the folder name perfectly.
- Loose jars directly inside `.minecraft\versions` are still not enough; the launcher still needs
  a version folder and matching JSON metadata.

## Windows helper naming

- Active Windows helper scripts now use general `win` naming instead of `win7` naming:
  - `scripts\run-win.cmd`
  - `scripts\build-win.cmd`
  - `scripts\boot-card-win.txt`
  - `docs\WINDOWS.md`
- This better reflects that the normal Windows path is intended for Windows 7 and newer, not only
  Windows 7.

## Package

Download the attached release asset:

```text
MCLauncherRevival-v0.5.8-alpha.zip
```

Use the attached GitHub Releases ZIP, not GitHub's green **Code -> Download ZIP** source archive.

## Status

This is still alpha / experimental software.

- Unofficial project.
- Not affiliated with Mojang, Microsoft, Xbox, or Minecraft.
- Not a replacement for the official Minecraft Launcher.
- Intended for preservation, nostalgia, and learning.
- Use at your own risk.
