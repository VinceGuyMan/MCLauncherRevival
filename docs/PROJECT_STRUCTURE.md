# Project Structure

This repo is intentionally small, old-Windows friendly, and mostly source-first. The root keeps the
human-facing launchers and documentation visible, while internal helpers live under `scripts/`.

```text
src/                         Java launcher source
resources/net/minecraft/     Images bundled into the launcher jar
docs/                        Documentation, screenshots, release notes
scripts/                     Build, run, and package helper scripts
tools/                       Dependency helper scripts and optional local Java payloads
.github/workflows/           GitHub Actions build workflow
```

## Important root files

```text
Setup MCLauncherRevival.cmd              Recommended setup/launcher hub
Start MCLauncherRevival.cmd              Friendly Windows entrypoint
Start MCLauncherRevival XP Offline.cmd   XP offline/classic entrypoint
README.md                                Main GitHub project page
MODERNIZATION.md                         Technical modernization notes
SECURITY.md                              Auth/token safety notes
NOTICE.md                                Notices and ownership notes
LICENSE                                  License for original modernization code/scripts
```

## Helper scripts

```text
scripts/run-win7.cmd                     Windows 7-11 runtime helper
scripts/build-win7.cmd                   Java build helper
scripts/package-release.cmd              Normal release ZIP builder
scripts/package-xp-release.cmd           Optional XP bundled-Java ZIP builder
scripts/run-linux.sh                     Preliminary Linux runtime helper
scripts/build-linux.sh                   Preliminary Linux build helper
scripts/run-macos.sh                     Preliminary macOS runtime helper
scripts/build-macos.sh                   Preliminary macOS build helper
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
```

The jar should be attached to a GitHub Release rather than committed to normal source history.