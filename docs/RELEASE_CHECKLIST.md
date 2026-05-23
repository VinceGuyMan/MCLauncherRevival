# Release Checklist

For `v0.7.0` and later alpha builds:

- Confirm the project builds from a clean clone.
- Confirm the jar launches.
- Confirm Java bytecode remains major version `51`.
- Confirm offline mode behavior.
- Confirm `Style: Auto` maps Beta, Alpha, Infdev, Classic, and Pre-Classic versions to the expected
  recreated layouts.
- Confirm manual `Style:` overrides persist after restart.
- Confirm the animated yellow splash text runs on Update Notes and pauses on Launcher Log/Profile
  Editor.
- Confirm the RAM presets launch with the expected JVM settings, especially `Air 64MB`,
  `Potato 256MB`, and `Overkill 8192MB`.
- Confirm `resources/net/minecraft/themes/` is included in the built jar and release ZIP.
- Confirm Microsoft sign-in flow does not request passwords inside the app.
- Confirm the release ZIP is used for user testing, not GitHub's source-code ZIP.
- Confirm Windows 7 dependency setup can reuse `tools\jdk8` or `tools\temurin8-jdk.zip`.
- Confirm the Microsoft login prompt opens before the browser and supports redirect paste fallback.
- Confirm the XErr `2148916233` troubleshooting note points users to Xbox profile setup.
- Confirm `Redownload Version` refuses empty or path-like version names and only deletes the
  selected `.minecraft\versions\<version>` folder.
- Confirm Linux shell scripts pass syntax checks and are clearly marked as preliminary until tested
  on a real distro.
- On Linux, run `bash -n scripts/run-linux.sh scripts/build-linux.sh` when a machine with bash is
  available.
- On Linux, manually smoke-test `java -jar MCLauncherRevival.jar` only as UI/preliminary behavior
  unless old-client game launch is tested.
- Confirm macOS shell scripts pass syntax checks on a real Mac.
- On macOS, run `sh -n build-macos.sh run-macos.sh package-macos.sh scripts/run-macos.sh scripts/build-macos.sh`.
- On macOS, run `./build-macos.sh` and confirm `MCLauncherRevival.jar` exists.
- On macOS, run `java -jar MCLauncherRevival.jar --smoke-test`.
- On macOS, run `./package-macos.sh` and confirm `dist/MCLauncherRevival.app` exists.
- Confirm `Start MCLauncherRevival.command` uses LF line endings and can call `run-macos.sh`.
- Confirm b1.6.6 and b1.7.3 can reach a visible title screen on macOS when a compatible Java 8
  runtime and Mojang version files are available.
- Confirm the macOS local color-correction cache is generated under
  `~/Library/Application Support/minecraft/launcher_revive/runtime/color-fix` and is not committed.
- Confirm remaining macOS old-client/LWJGL limitations are documented.
- Confirm saved tokens/settings can be removed if implemented.
- Confirm no secrets, tokens, client secrets, or personal credentials are committed.
- Confirm README instructions match the actual release files.
- Confirm release zip contains only expected files.
- Confirm `scripts/package-release.cmd` fails if `MCLauncherRevival.jar` is missing.
- Confirm `scripts/package-release.cmd` prints the final ZIP contents.
- Confirm the final ZIP includes `MCLauncherRevival.jar`.
- Confirm antivirus false-positive risk is minimized by avoiding packed/obfuscated binaries.
- Attach release zip to GitHub Releases manually or through the GitHub CLI.
- Mark the release clearly as alpha.

## Expected source files

Keep source history focused on readable source, scripts, docs, and lightweight launcher resources.

Expected project files include:

- `src/`
- `resources/`
- `docs/`
- `.github/workflows/build.yml`
- `Setup MCLR.cmd`
- `Start MCLR.cmd`
- `Start MCLR XP.cmd`
- `Start MCLauncherRevival.command`
- `build-macos.sh`
- `run-macos.sh`
- `package-macos.sh`
- `scripts/run-win.cmd`
- `scripts/build-win.cmd`
- `scripts/run-linux.sh`
- `scripts/build-linux.sh`
- `scripts/run-macos.sh`
- `scripts/build-macos.sh`
- `scripts/package-release.cmd`
- `scripts/package-xp-release.cmd`
- `tools/download-temurin8-jdk.ps1`
- `README.md`
- `CHANGELOG.md`
- `CONTRIBUTING.md`
- `SECURITY.md`
- `NOTICE.md`
- `docs/TRUST_AND_SAFETY.md`
- `docs/DISCLAIMER.md`
- `docs/AUTH_FLOW.md`
- `docs/HISTORICAL_THEMES.md`
- `docs/RELEASES.md`
- `docs/MACOS.md`
- `docs/RELEASE_NOTES_v0.7.0.md`
- `LICENSE`

## Do not commit

- Build output jars.
- Release zip files.
- Downloaded JDKs.
- Unsigned app bundles under `dist/`.
- `.minecraft` folders.
- OAuth tokens, auth caches, local settings, or secrets.
- Packed/obfuscated binaries.

## Current alpha release name

Recommended release tag and artifact names:

```text
v0.7.0
MCLauncherRevival-v0.7.0-alpha.zip
MCLauncherRevival.jar
```
