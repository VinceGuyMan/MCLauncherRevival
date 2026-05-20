# Project Structure

This repo is intentionally small and old-Windows friendly.

```text
src/                         Java launcher source
resources/net/minecraft/     Images bundled into the launcher jar
docs/                        Documentation, screenshots, release notes
tools/                       Helper scripts
.github/workflows/           GitHub Actions build workflow
```

## Important root files

```text
Start MCLauncherRevival.cmd              Friendly Windows entrypoint
Start MCLauncherRevival XP Offline.cmd   XP offline/classic entrypoint
run-win7.cmd                             Runtime helper
build-win7.cmd                           Java build helper
run-linux.sh                             Preliminary Linux runtime helper
build-linux.sh                           Preliminary Linux build helper
README.md                                Main GitHub project page
MODERNIZATION.md                         Technical modernization notes
SECURITY.md                              Auth/token safety notes
NOTICE.md                                Notices and ownership notes
LICENSE                                  License for original modernization code/scripts
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
tools/jdk8/
tools/temurin8-jdk.zip
```

The jar should be attached to a GitHub Release rather than committed to normal source history.

