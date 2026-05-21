# MCLauncherRevival v0.5.5-alpha Release Notes

v0.5.5-alpha is a historical-notes and presentation polish release. It builds on the v0.5.0
historical launcher style system with better in-app version notes, more splash text variety, and
GitHub-ready showcase screenshots.

## Highlights

- Added static, offline-friendly historical notes for major launcher-visible Beta, Alpha, Infdev,
  Classic, and Pre-Classic versions.
- Update Notes now prefer exact version entries first and clearly label family fallbacks when exact
  notes are unavailable.
- Patch Notes Mode now shows compact `Added`, `Changed`, `Fixed`, `Removed`, `Known quirks`, and
  `Sources` sections.
- Added source links to the in-launcher notes where possible.
- Added `docs/VERSION_NOTES.md` explaining where notes come from and how to add more safely.
- Expanded the animated splash text pool with 50 additional nostalgia/auth/old-Java themed phrases.
- Changed Beta-era Update Notes headers to `Minecraft Beta News` so Beta builds match the
  era-specific naming used by the other historical layouts.
- Added GitHub showcase screenshots for our recreated take on each historical build-era style:
  Infdev/prototype, Pre-Classic, Classic, Alpha, and Beta.

## Historical notes policy

The launcher ships static notes data and does not scrape historical pages at runtime. Notes are
summarized from public historical references such as Mojang's official version manifest, Minecraft
Wiki version pages, and Minecraft Timeline orientation.

If exact historical notes for a selected build are not available, the launcher now says so directly
and shows the nearest verified family summary instead.

## Asset policy

The era screenshots and theme textures are project-owned recreated interpretations. They are not
bundled original Mojang/Microsoft launcher assets.

## Compatibility notes

- Windows 7-11 remains the primary full-auth target.
- Windows XP remains offline/classic focused.
- macOS and Linux remain experimental for old Minecraft/LWJGL game launch.
- The launcher should never ask for a raw Microsoft password inside the app.

## Package

Download the attached GitHub Releases asset:

```text
MCLauncherRevival-v0.5.5-alpha.zip
```

Do not use GitHub's green **Code -> Download ZIP** button for normal play. Source ZIPs may not
include the built jar or runnable package layout.
