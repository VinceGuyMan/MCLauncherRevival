# MCLauncherRevival v0.5.0-alpha Release Notes

v0.5.0-alpha is a visual authenticity pass. The launcher now changes more than its background:
selected versions can drive recreated launcher-era layouts while the same modern backend continues
to handle Microsoft OAuth, offline play, logging, version selection, and launch safety.

## Highlights

- Added `Style: Auto`, `Beta`, `Alpha`, `Infdev`, `Classic`, and `Pre-Classic`.
- `Auto` resolves the visual style from the selected version family.
- Added recreated era textures under `resources/net/minecraft/themes/`.
- Added animated yellow splash text inspired by Minecraft title-screen splash motion.
- Added theme-specific sidebars, tab labels, titles, update-note panels, and launcher notes.
- Added `Low-end 384MB` for older or weaker systems.
- Cached tiled theme backgrounds and avoided unnecessary Update Notes reloads.

## Version-to-style mapping

| Version pattern | Auto style |
| --- | --- |
| `b*` | Beta |
| `a*` | Alpha |
| `inf-*` | Infdev |
| `c0.*` | Classic |
| `rd-*` | Pre-Classic |

## Asset policy

Historical launcher screenshots were used only as visual references. The release package does not
bundle extracted Mojang/Microsoft launcher assets or Minecraft game files. Included theme textures
are recreated project-owned assets.

## Compatibility notes

- Windows 7-11 remains the primary full-auth target.
- Windows XP remains offline/classic focused.
- macOS and Linux remain experimental for old Minecraft/LWJGL game launch.
- The launcher should never ask for a raw Microsoft password inside the app.

## Package

Download the attached GitHub Releases asset:

```text
MCLauncherRevival-v0.5.0-alpha.zip
```

Do not use GitHub's green **Code -> Download ZIP** button for normal play. Source ZIPs may not
include the built jar or runnable package layout.
