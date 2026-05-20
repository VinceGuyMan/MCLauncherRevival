# Changelog

## v0.3.5-alpha

- Confirmed XP Offline mode launches successfully on real Windows XP hardware with an XP-compatible
  Java runtime. Performance is hardware-dependent; one test system ran around 15 FPS on lowest
  settings.
- Added XP field-test screenshots showing the test machine specs and prepared `.minecraft` folder
  transfer workflow from a newer PC to removable media to Windows XP.
- The launcher now scans `.minecraft\versions` on startup and adds locally prepared versions to
  the dropdown, which helps XP show versions that were copied from a newer PC.
- Added a macOS compatibility warning before launching old Beta/Alpha clients.
- Added macOS Launcher Log/Profile notes explaining blank-window LWJGL/OpenGL/native compatibility
  risks and where to inspect `last-launch.log`.
- Updated macOS docs to mark game launch as experimental even when the launcher UI opens.
- Added XP/OpenGL troubleshooting for `Pixel format not accelerated` and missing accelerated
  OpenGL mode errors.
- Launcher Log now explains detected LWJGL OpenGL acceleration failures as XP graphics driver
  issues instead of auth or version-file issues.
- XP Play Offline now checks for local selected-version files before trying modern HTTPS downloads.
- XP TLS/certificate failures such as `handshake_failure`, `Received fatal alert`, `PKIX`, and
  `unable to find valid certification path` now show a friendly offline-prep message.
- XP mode now detects loose `.minecraft\versions\<version>.jar` files and explains the required
  Mojang launcher-style `versions\<version>\<version>.jar` plus JSON/libraries/assets layout.
- Added XP version setup docs, including the preferred Windows 7+ copy workflow and optional
  MCVersions.net client-jar guidance with ownership/legal reminders.
- Added XP notes to Launcher Log/Profile Editor explaining that Microsoft login and fresh downloads
  are disabled/best-effort and offline play needs pre-cached `.minecraft` files.
- Added docs for preparing Minecraft versions on Windows 7 or newer and copying `versions`,
  `libraries`, and `assets` to Windows XP.
- Clarified release-package docs so users do not accidentally use GitHub source-code or tag ZIPs
  instead of the attached release asset.
- Added `package-release.cmd` to build, stage, zip, list, and verify alpha release packages.
- Added `package-xp-release.cmd` for maintainer-created XP bundled-Java packages that include only
  a manually supplied `tools\java7` runtime.
- XP startup now prefers `tools\java7\bin\java.exe` in XP mode and clearly points missing-Java users
  to the XP bundled-Java package or a manual `tools\java7` runtime.
- XP bundled-Java packages can now include local maintainer-supplied installer EXEs under
  `tools\java-installers`, and XP startup asks before running one if Java is missing.
- Added a manual XP Java setup guide for releases that cannot redistribute Java files publicly.
- Improved XP-mode missing-jar output so it explains source ZIP vs release asset confusion and
  stops cleanly instead of trying to build or download Java.

## v0.2.5

- Added preliminary Linux `run-linux.sh` and `build-linux.sh` wrappers.
- Added Linux compatibility documentation with honest testing limits and runtime requirements.
- Added preliminary macOS `run-macos.sh` and `build-macos.sh` wrappers.
- Added macOS compatibility documentation with honest testing limits and runtime requirements.
- Fixed XP offline startup scripts so XP mode no longer depends on `choice.exe`.
- XP mode now avoids the Temurin/Adoptium auto-download path by default and asks users to install or
  extract Java 7 or an XP-compatible Java 8 runtime manually.
- XP release packages now run an existing `MCLauncherRevival.jar` with `java.exe` without requiring
  `javac.exe` or `jar.exe` unless a source build is actually needed.
- Added Windows XP troubleshooting notes for missing `choice.exe`, Java runtime setup, and the
  limits of modern login/downloads on XP.

## v0.2.0-alpha

### Release packaging and Windows 7 testing

- Prepared a cleaner alpha release package based on the tested GitHub Releases ZIP workflow.
- Clarified that users should download the attached release ZIP, not GitHub's green
  **Code -> Download ZIP** source archive.
- Documented Windows 7 field-test results for Microsoft login, redirect paste fallback behavior,
  and XSTS account setup issues.
- Added troubleshooting steps for `XErr: 2148916233`, which usually means the Microsoft account
  still needs an Xbox profile before the Xbox Live/XSTS/Minecraft services chain can continue.
- Reconfirmed that Offline Play remains available when online authentication fails.
- Kept account-safety wording clear: the launcher should never ask for a raw Microsoft password
  inside the app.

## v0.1.1-alpha patch notes

### Windows 7 field test notes

Windows 7 field testing confirmed that MCLauncherRevival works best when using the attached GitHub
Releases ZIP package instead of GitHub's green **Code -> Download ZIP** source archive. The source
archive does not necessarily include the same built jar/package layout as the release asset.

Microsoft login can complete on Windows 7, but the account still needs a valid Xbox profile before
the Xbox Live/XSTS/Minecraft services chain can continue. If login reaches XSTS authorization and
fails with:

```text
XSTS authorization failed (HTTP 401)
XErr: 2148916233
Redirect: https://start.ui.xboxlive.com/
```

use this fix:

1. Open <https://start.ui.xboxlive.com/>.
2. Finish Xbox profile setup for the Microsoft account.
3. Return to MCLauncherRevival.
4. Click `Forget Login`.
5. Try `Microsoft Login` again.

Windows 7 may also need the browser redirect URL paste fallback because Microsoft can change the
post-login redirect URL to `removed=true` shortly after login. The launcher should never ask for a
raw Microsoft password inside the app. Offline Play remains available when online authentication
fails.

## Earlier alpha changes

- Fixed Windows 7 Java dependency setup by forcing TLS 1.2 before downloading Temurin 8 from
  Adoptium.
- Improved JDK setup fallback behavior so an existing `tools\jdk8` install or local
  `tools\temurin8-jdk.zip` is used before downloading again.
- Changed Microsoft login opening to use the user's default browser instead of the old Internet
  Explorer COM browser helper.
- Made the Microsoft login paste prompt appear before the browser opens and added clipboard fallback
  detection for copied OAuth redirect URLs.
- Rebuilt the February 2011-style launcher as a small Java launcher compiled as Java 7 bytecode.
- Added Windows XP offline/classic mode support.
- Replaced legacy Mojang username/password login with Microsoft browser OAuth.
- Added Xbox Live, XSTS, Minecraft services login, and profile lookup.
- Added local token cache with `Forget Login`.
- Kept offline singleplayer launch mode.
- Added Beta 1.8.x and older version selection from Mojang metadata.
- Added version download/redownload support.
- Added launcher log and profile editor utility pages.
- Added save backup, texture pack import, folder shortcuts, Java warning, and version install
  status.
- Preserved the dirt-background news panel and classic compact launcher layout.

