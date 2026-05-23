# Project Structure

This repo is intentionally small, old-Windows friendly, and mostly source-first. The root keeps the
human-facing launchers and documentation visible, while internal helpers live under `scripts/`.

```text
src/                         Java launcher source
resources/net/minecraft/     Images bundled into the launcher jar
resources/net/minecraft/themes/
                             Recreated era-layout background textures
docs/                        Documentation, screenshots, release notes
scripts/                     Build, run, and package helper scripts
tools/                       Dependency helper scripts and optional local Java payloads
.github/workflows/           GitHub Actions build workflow
```

## Important root files

```text
Setup MCLR.cmd              Recommended setup/launcher hub
Start MCLR.cmd              Friendly Windows entrypoint
Start MCLR XP.cmd           XP offline/classic entrypoint
Start MCLauncherRevival.command       Friendly macOS Finder entrypoint
build-macos.sh                         Root macOS build wrapper
run-macos.sh                           Root macOS run wrapper
package-macos.sh                       Root unsigned macOS app-bundle packager
README.md                                Main GitHub project page
MODERNIZATION.md                         Technical modernization notes
SECURITY.md                              Auth/token safety notes
NOTICE.md                                Notices and ownership notes
LICENSE                                  License for original modernization code/scripts
```

## Helper scripts

```text
scripts/run-win.cmd                     Windows 7-11 runtime helper
scripts/build-win.cmd                   Java build helper
scripts/package-release.cmd              Normal release ZIP builder
scripts/package-xp-release.cmd           Optional XP bundled-Java ZIP builder
scripts/run-linux.sh                     Preliminary Linux runtime helper
scripts/build-linux.sh                   Preliminary Linux build helper
scripts/run-macos.sh                     macOS runtime helper used by root wrapper
scripts/build-macos.sh                   macOS build helper used by root wrapper
```

## Key docs

```text
docs/MACOS.md                           macOS build, run, package, and troubleshooting notes
docs/LINUX.md                           Linux preliminary testing notes
docs/WINDOWS.md                         Windows 7-11 notes
docs/WINDOWS_XP.md                      XP offline/classic notes
docs/RELEASES.md                        Release package layout
docs/RELEASE_CHECKLIST.md               Manual release validation checklist
docs/RELEASE_NOTES_v0.7.0.md            v0.7.0-alpha release notes
```

## Source

```text
src/net/minecraft/
```

Contains the Java launcher implementation:

- Microsoft OAuth/Xbox/XSTS/Minecraft services login.
- Beta/Alpha version download and launch logic.
- Token and launcher settings cache.
- Classic Swing UI.
- Version notes and splash text.

## Resources

```text
resources/net/minecraft/
```

These files are copied into the jar under `/net/minecraft/` at build time so existing resource
lookups remain simple.

The `themes/` subfolder contains small recreated pixel textures used by the v0.5.0 historical
launcher layouts. They are project-owned recreated assets, not extracted Mojang/Microsoft launcher
or game files.

## Screenshots

```text
docs/screenshots/
```

Contains README screenshots, annotated UI guides, tab captures, login-flow captures, and social
preview artwork.

## Generated/local files

These should not be committed:

```text
build/
MCLauncherRevival.jar
dist/
release/
tools/jdk8/
tools/temurin8-jdk.zip
tools/temurin8-jdk*.tar.gz
```

The jar should be attached to a GitHub Release rather than committed to normal source history.

On macOS, old-client launches can also generate local helper/cache files under:

```text
~/Library/Application Support/minecraft/launcher_revive/runtime/
```

Those runtime files include the foreground game helper, private launch config, and local
color-correction jar copies made from the user's own downloaded game files. They should not be
committed or packaged as project source.
