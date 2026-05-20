# Changelog

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

## Unreleased / v0.1.0-alpha prep

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

