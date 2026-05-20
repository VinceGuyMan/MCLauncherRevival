# GitHub Release Checklist

Use this before posting the project to GitHub.

## Include in source repository

- `src/`
- `net/minecraft/dirt.png`
- `net/minecraft/logo.png`
- `net/minecraft/favicon.png`
- `Block.png`
- `scrolls.png`
- `cobalt.png`
- `StevePlaceholder.jpg`
- `build-win7.cmd`
- `run-win7.cmd`
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

Windows 7 through Windows 11 are the primary targets. Java 8 is recommended.
```
