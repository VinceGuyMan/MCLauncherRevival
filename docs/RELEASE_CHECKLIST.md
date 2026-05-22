# Release Checklist

For `v0.5.6`:

- Confirm the project builds from a clean clone.
- Confirm the jar launches.
- Confirm Java bytecode remains major version `51`.
- Confirm offline mode behavior.
- Confirm `Style: Auto` maps Beta, Alpha, Infdev, Classic, and Pre-Classic versions to the expected
  recreated layouts.
- Confirm manual `Style:` overrides persist after restart.
- Confirm the animated yellow splash text runs on Update Notes and pauses on Launcher Log/Profile
  Editor.
- Confirm the `Low-end 384MB` memory preset launches with the expected `-Xmx384M` JVM setting.
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
- Confirm macOS shell scripts pass syntax checks and are clearly marked as preliminary until tested
  on a real Mac.
- On macOS, run `sh -n scripts/run-macos.sh scripts/build-macos.sh` and verify the blank-window
  limitation remains documented.
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
- `scripts/run-win7.cmd`
- `scripts/build-win7.cmd`
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
- `docs/RELEASE_NOTES_v0.5.6.md`
- `LICENSE`

## Do not commit

- Build output jars.
- Release zip files.
- Downloaded JDKs.
- `.minecraft` folders.
- OAuth tokens, auth caches, local settings, or secrets.
- Packed/obfuscated binaries.

## Current alpha release name

Recommended release tag and artifact names:

```text
v0.5.0
MCLauncherRevival-v0.5.6-alpha.zip
MCLauncherRevival.jar
```


