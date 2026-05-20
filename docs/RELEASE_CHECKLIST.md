# GitHub Release Checklist

Use this before posting the project to GitHub.

## Include in source repository

- `src/`
- `resources/net/minecraft/dirt.png`
- `resources/net/minecraft/logo.png`
- `resources/net/minecraft/favicon.png`
- `resources/net/minecraft/Block.png`
- `resources/net/minecraft/scrolls.png`
- `resources/net/minecraft/cobalt.png`
- `resources/net/minecraft/StevePlaceholder.jpg`
- `build-win7.cmd`
- `run-win7.cmd`
- `Start MCLauncherRevival.cmd`
- `Start MCLauncherRevival XP Offline.cmd`
- `tools/download-temurin8-jdk.ps1`
- `README.md`
- `MODERNIZATION.md`
- `SECURITY.md`
- `NOTICE.md`
- `LICENSE`
- `CHANGELOG.md`
- `CONTRIBUTING.md`
- `.gitignore`
- `.gitattributes`
- `.github/workflows/build.yml`
- `docs/WINDOWS_7.md`
- `docs/WINDOWS_XP.md`
- `docs/AUTH_FLOW.md`
- `docs/RELEASES.md`
- `docs/DISCLAIMER.md`
- `docs/PROJECT_STRUCTURE.md`
- `docs/screenshots/frontpage.png`
- `docs/screenshots/social-preview.png`

## Do not include in source repository

- `tools/jdk8/`
- `tools/temurin8-jdk.zip`
- `build/`
- `.svn/`
- `%APPDATA%\.minecraft\launcher_revive\auth.properties`
- Any downloaded `.minecraft` versions/libraries.

## Release artifact

Build locally or through GitHub Actions:

```bat
build-win7.cmd
```

Attach this file to a GitHub Release:

```text
MCLauncherRevive-modern.jar
```

Do not commit the jar to the source repository unless you intentionally want to track binary releases in git.

## Release description starter

```text
MCLauncherRevive modernizes a classic February 2011-style Minecraft launcher with Microsoft OAuth, Xbox Live/XSTS/Minecraft services authentication, offline mode, and Beta/Alpha version launching while preserving the old dirt-background launcher vibe.

MCLauncherRevive supports Windows XP for offline/classic play and Windows 7 through Windows 11 for the full modern-auth launcher experience. Java 7 or an XP-compatible Java 8 build is recommended for XP; Java 8 is recommended on Windows 7 and newer.
```
