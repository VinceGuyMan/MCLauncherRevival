# MCLauncherRevival v0.5.8.1-alpha Release Notes

v0.5.8.1-alpha is a small patch release focused on fixing Microsoft Login after the v0.5.8
trust-flow changes.

## Microsoft login fix

- Fixed Microsoft Login opening an invalid localhost redirect with the default public client ID.
- The default login path now uses Microsoft's registered desktop redirect again.
- Loopback callback login is still available only for custom registered Microsoft client IDs with
  matching loopback redirect registration.
- The trust dialog, PKCE/state validation, and paste-back fallback remain in place.
- The broken visible code-login/device-code button has been removed from current builds.

## Launcher polish included

- Renamed the old-machine checkbox from `Low-End` to `Potato Mode!`.
- Expanded the RAM preset dropdown:
  - `Air 64MB`
  - `Rock 128MB`
  - `Potato 256MB`
  - `Low-end 384MB`
  - `Classic 512MB`
  - `Comfort 1024MB`
  - `Modern 2048MB`
  - `Gamer 4096MB`
  - `Overkill 8192MB`
  - `Custom`

## Package

Download the attached release asset:

```text
MCLauncherRevival-v0.5.8.1-alpha.zip
```

Use the attached GitHub Releases ZIP, not GitHub's green **Code -> Download ZIP** source archive.

## Status

This is still alpha / experimental software.

- Unofficial project.
- Not affiliated with Mojang, Microsoft, Xbox, or Minecraft.
- Not a replacement for the official Minecraft Launcher.
- Intended for preservation, nostalgia, and learning.
- Use at your own risk.
