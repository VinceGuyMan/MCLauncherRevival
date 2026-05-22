# Changelog

## Unreleased

## v0.5.7-alpha

- Renamed the user-facing Windows launcher shortcuts to shorter MCLR filenames: `Setup MCLR.cmd`, `Start MCLR.cmd`, and `Start MCLR XP.cmd`.
- Reworked the first-run welcome panel into an old launcher-style `Minecraft News` update post and removed the duplicate Indev notes sidebar link.
- Added a cleaner retro CMD startup screen before the Swing launcher opens, including the large MCLauncherRevival ASCII banner and simple Windows/XP boot cards.
- Removed UTF-8 BOM issues from Windows batch launchers so CMD no longer prints raw `@echo off` errors before startup.
- Kept root starter scripts quiet and moved the visual startup flow into `scripts\run-win7.cmd`, guarded by `MCLR_BANNER_SHOWN` so nested calls do not duplicate the banner.
- Simplified startup status output to short `[ OK ]`, `[WARN]`, `[INFO]`, `[ XP ]`, and `[FAIL]` messages ending with `Press Nothing. Happy Mining!`.
- Added `--verbose` / `MCLR_VERBOSE=1` support for troubleshooting without making normal startup noisy.
- Improved Windows Java detection for nested portable installs such as `tools\jdk8\jdk8u492-b09`, and made the missing-Java prompt default to downloading portable Java 8 when Enter is pressed.
- Fixed the release jar manifest so `java -jar MCLauncherRevival.jar` starts the real Swing launcher entrypoint instead of the backend launch helper.
- Updated release packaging to use a private temporary staging folder so Explorer locks on extracted release folders do not corrupt or confuse new ZIP builds.
- Improved Windows Java detection in the run/build scripts. They now check bundled Java folders,
  `JAVA_HOME`, PATH, and common `Program Files\Java` install folders before offering the portable
  Temurin 8 JDK download, which now defaults to yes on Windows 7+ when Java is still missing.
- Fixed the setup hub closing immediately after choosing an option by keeping the setup window open
  at the end and preserving visible exit codes from the Windows launcher shortcuts.
- Tightened historical era layouts so the right sidebar is modular by selected style instead of
  sharing one modern link stack. Beta keeps the full news/links/games rail, Alpha is a shorter
  login-board style rail, Indev/Infdev reads more like a devlog panel, Classic has only a tiny
  early-web link box, and Pre-Classic is reduced to a prototype test panel.
- Added more precise Microsoft/Xbox auth error text for common XSTS failures, including missing
  Xbox profile setup, child/family-managed accounts, region/account issues, and missing Java
  Edition profile ownership.
- Added a Low-End mode toggle for older machines. It applies 384MB memory, compact notes, disables
  the animated splash, and uses a smaller launcher window size.
- Removed the exact/fallback color squares from the historical notes panel now that the
  launcher-visible version notes have been filled in.
- Added clearer selected-version readiness text so the launcher can distinguish missing
  jar/json files, libraries, natives, and ready-to-run local versions.
- Added an XP version preparation help link in the Profile Editor and improved missing
  library/native diagnostics during launch preparation.
- Fixed Alpha and Pre-Classic right-side layout spacing so the sidebar text stays inside the
  launcher frame and the Scrolls/Cobalt logo block has enough room to sit centered.

## v0.5.6-alpha

- Added exact static note entries for more launcher-visible maintenance/reupload builds so fewer
  selected versions need broad family fallbacks.
- Tightened historical era presentation: Alpha is more compact/login-board flavored, Indev/Infdev
  is more prototype/dev-note flavored, and Classic / Pre-Classic use smaller, simpler chrome.
- Expanded the first-run guide with clearer online/offline/XP/release-ZIP guidance.
- Added a Profile Editor link to reopen the first-run guide.
- Added small control tooltips for Microsoft Login, Play Offline, Style, memory, redownload, and
  token-clearing actions without changing the classic visual layout.

## v0.5.5-alpha

- Expanded the animated splash text pool with 50 additional nostalgia/auth/old-Java themed phrases.
- Updated Beta-era Update Notes headers to say `Minecraft Beta News` so Beta builds match the
  era-specific naming used by the other historical layouts.
- Added GitHub showcase screenshots for our recreated take on each historical build-era style:
  Infdev/prototype, Pre-Classic, Classic, Alpha, and Beta.
- Added static, offline-friendly historical version notes for major launcher-visible Beta, Alpha,
  Infdev, Classic, and Pre-Classic versions.
- Update Notes now prefer exact version entries and clearly label family fallbacks when exact notes
  are not available.
- Patch Notes Mode now shows compact Added / Changed / Fixed / Removed / Known quirks sections with
  source links.
- Added `docs/VERSION_NOTES.md` to document note sources and safe rules for adding more history.

## v0.5.0-alpha

- Added a historical launcher presentation system with `Auto`, `Beta`, `Alpha`, `Infdev`,
  `Classic`, and `Pre-Classic` style modes.
- `Auto` style now maps selected versions to recreated era layouts:
  - `b*` versions resolve to the Beta-style launcher.
  - `a*` versions resolve to the Alpha-style launcher.
  - `inf-*` versions resolve to the Infdev-style launcher.
  - `c0.*` versions resolve to the Classic-style launcher.
  - `rd-*` versions resolve to the Pre-Classic-style launcher.
- Added a `Style:` dropdown in the bottom control bar. The selected style is saved in local launcher
  settings and shown in the Launcher Log / Profile Editor pages.
- Reworked the Update Notes presentation so version notes are content-driven while the active era
  layout controls the surrounding visual style, sidebar, panel tone, and title/tab labels.
- Added recreated, repo-owned pixel textures under `resources/net/minecraft/themes/` for the new
  era layouts. These are original project assets, not extracted Mojang/Microsoft launcher files.
- Added Minecraft-style animated yellow splash text that floats, scales, and tilts on the Update
  Notes page, then pauses outside the news view.
- Improved HTML link readability across dark era textures with a subtle backing and bottom-edge
  treatment while keeping the old blue-link feel.
- Added GitHub showcase screenshots for our recreated take on each historical build-era style:
  Infdev/prototype, Pre-Classic, Classic, Alpha, and Beta.
- Added a `Low-end 384MB` memory preset for weaker/period-correct hardware.
- Cached theme background tiles and avoided unnecessary news HTML reloads to reduce UI churn.
- Updated build scripts so nested resources, including theme textures, are copied into
  `MCLauncherRevival.jar`.
- Added v0.5.0 release documentation describing the historical layout work and safe asset policy.


## v0.4.6-alpha

- Moved internal build, run, and packaging helpers into `scripts/` to keep the GitHub root cleaner.
- Kept the main double-click Windows entrypoints in the repo root for normal users.
- Updated documentation, GitHub Actions, and release packaging paths for the new script layout.

## v0.4.5-alpha

- Added `Setup MCLR.cmd`, a single Windows setup hub that auto-detects or asks for the
  right path, then routes users to Windows 7-11 normal launch, XP Offline / Classic mode, source
  builds, or setup notes.
- Included the setup hub in standard and XP bundled-Java release package staging.
- Setup hub now preserves child script exit codes for clearer automation/package testing.
- Improved the XSTS no-Xbox-profile message with the Xbox profile setup URL and retry steps.
- XP mode now reports loaded local versions even when exactly one prepared version is found.
- Microsoft browser login now requests an authorization code with `offline_access` so Microsoft
  refresh tokens can be cached when the account flow returns them.
- Hardened `Redownload Version` so it can only delete the selected folder directly under
  `.minecraft\versions`.
- Windows run script now preserves the Java launcher process exit code for setup/package checks.
- XP build-script wording now points users to `tools\java7` first, with `tools\jdk8` as a fallback.
- Added best-effort macOS/Linux browser fallbacks for Microsoft login after Java Desktop browsing.
- Made OpenGL/LWJGL launch-log diagnostics platform-neutral instead of XP-only.
- macOS run script now gives a clearer source-ZIP warning and invokes the build script through `sh`.
- Expanded Windows 7+, XP, macOS, Linux, auth-flow, and release checklist documentation based on the
  four-agent platform audit.

## v0.4.0-alpha

- Added more XP field-test performance notes for Beta 1.4, Alpha 1.2.6, and Alpha 1.2_01.
- Added XP field-test screenshots showing the test machine specs and prepared `.minecraft` folder
  transfer workflow from a newer PC to removable media to Windows XP.
- Added Windows XP Beta 1.7.3 proof-of-launch screenshots showing the main menu and debug/FPS view.

## v0.3.5-alpha

- Confirmed XP Offline mode launches successfully on real Windows XP hardware with an XP-compatible
  Java runtime. Performance is hardware-dependent; one test system ran around 15 FPS on lowest
  settings.
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


## Unreleased - Historical version notes

- Added static, offline-friendly historical notes for major launcher-visible Beta, Alpha, Infdev, Classic, and Pre-Classic versions.
- Update Notes now prefer exact version entries and clearly label family fallbacks when exact notes are not available.
- Patch Notes Mode now shows compact Added / Changed / Fixed / Removed / Known quirks sections with source links.
- Added `docs/VERSION_NOTES.md` to document note sources and safe rules for adding more history.
