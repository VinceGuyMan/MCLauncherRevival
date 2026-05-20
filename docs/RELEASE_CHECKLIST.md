# Release Checklist

For `v0.1.0-alpha`:

- Confirm the project builds from a clean clone.
- Confirm the jar launches.
- Confirm offline mode behavior.
- Confirm Microsoft sign-in flow does not request passwords inside the app.
- Confirm saved tokens/settings can be removed if implemented.
- Confirm no secrets, tokens, client secrets, or personal credentials are committed.
- Confirm README instructions match the actual release files.
- Confirm release zip contains only expected files.
- Confirm antivirus false-positive risk is minimized by avoiding packed/obfuscated binaries.
- Attach release zip to GitHub Releases manually.
- Mark the release clearly as alpha.

## Expected source files

Keep source history focused on readable source, scripts, docs, and lightweight launcher resources.

Expected project files include:

- `src/`
- `resources/`
- `docs/`
- `.github/workflows/build.yml`
- `Start MCLauncherRevival.cmd`
- `Start MCLauncherRevival XP Offline.cmd`
- `run-win7.cmd`
- `build-win7.cmd`
- `tools/download-temurin8-jdk.ps1`
- `README.md`
- `CHANGELOG.md`
- `CONTRIBUTING.md`
- `SECURITY.md`
- `NOTICE.md`
- `docs/TRUST_AND_SAFETY.md`
- `docs/DISCLAIMER.md`
- `docs/AUTH_FLOW.md`
- `docs/RELEASES.md`
- `LICENSE`

## Do not commit

- Build output jars.
- Release zip files.
- Downloaded JDKs.
- `.minecraft` folders.
- OAuth tokens, auth caches, local settings, or secrets.
- Packed/obfuscated binaries.

## First alpha release name

Recommended release tag and artifact names:

```text
v0.1.0-alpha
MCLauncherRevival-v0.1.0-alpha.zip
MCLauncherRevival.jar
```
