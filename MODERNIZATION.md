# MCLauncherRevival modernization notes

This project originally contains the February 2011 launcher unpacked as class files and resources.
The legacy login code posts a username and password to `https://login.minecraft.net/` and can
remember a password in `lastlogin`.

The modernized path does not edit the old bytecode. Instead, it adds Java source under
`src/net/minecraft` and builds a new jar that keeps the classic dirt background, logo, compact login
panel, news panel, and old launcher window proportions.

## What changed

- Legacy Mojang username/password login is bypassed.
- Microsoft browser OAuth is used instead of collecting a password.
- Authentication chain is:
  - Microsoft OAuth browser login
  - Xbox Live user authentication
  - XSTS authorization for Minecraft services
  - Minecraft services `login_with_xbox`
  - Minecraft profile lookup
- Refresh and access tokens are cached locally, never raw passwords.
- Offline singleplayer mode remains available without Microsoft login.
- Classic versions from Beta 1.8.x down are selectable from Mojang launch metadata.

## Token cache

The token cache is written to:

```text
%APPDATA%\.minecraft\launcher_revive\auth.properties
```

It stores OAuth refresh/access tokens and the latest Minecraft profile name/UUID. The launcher marks
the file user-readable/user-writable where Java exposes those permissions and hides it on Windows.
This is intentionally lightweight and does not store Microsoft passwords.

Use the launcher button `Forget Login` to delete cached tokens and leftover temporary macOS
game-launch credentials. The macOS helper deletes its one-use configuration immediately after
reading it. Local cleanup does not revoke an already-issued token at Microsoft.

## Windows build steps

Prerequisites:

- Windows XP for offline/classic play, or Windows 7 SP1 or newer for the full modern-auth flow.
- Java JDK 8 installed for building, or allow the `.cmd` scripts to download a portable Eclipse
  Temurin 8 JDK on supported systems.
- `javac.exe`, `jar.exe`, and `java.exe` available on `PATH`, or available from the local
  `tools\jdk8` folder created by the scripts.
- Java 8 update level new enough for TLS 1.2 HTTPS connections to Microsoft and Mojang when using
  online login/downloads.
- Java 7 or an XP-compatible Java 8 runtime for XP offline/classic mode.

Build:

```bat
scripts\build-win.cmd
```

Run:

```bat
scripts\run-win.cmd
```

If Java is missing, the scripts will offer to download the latest Eclipse Temurin 8 JDK from the
official Adoptium API:

```text
https://api.adoptium.net/v3/binary/latest/8/ga/windows/x64/jdk/hotspot/normal/eclipse
```

The download is portable and local to this project:

```text
tools\jdk8
```

It does not install Java system-wide.
The helper verifies Adoptium's published SHA-256 before extracting the archive. A cached archive is
reused only when it can be verified online or has its previously verified checksum sidecar.

The build creates:

```text
MCLauncherRevival.jar
```

You can also run it directly:

```bat
java -jar MCLauncherRevival.jar
```

## macOS build and run steps

macOS support is experimental, but the repo includes root-level helpers for normal users:

```sh
./build-macos.sh
./run-macos.sh
```

`build-macos.sh` compiles the Java sources into `build/classes`, copies resources, and creates
`MCLauncherRevival.jar` with `net.minecraft.MinecraftLauncher` as the main class. It prefers Java 8
when available, respects a valid `JAVA_HOME`, and fails with a clear message if no usable JDK is
available. It does not install Java automatically.

`run-macos.sh` builds the jar if needed, prefers Java 8, warns when running on a newer Java runtime,
and starts the launcher with macOS-friendly app-name/Dock properties.

For old LWJGL clients on macOS, the Play path uses a small locally built foreground app helper so
Minecraft appears as a normal window. It also generates local color-corrected copies of the user's
own downloaded Minecraft/LWJGL jars under `launcher_revive/runtime/color-fix` to work around the
red/blue channel swap observed on modern macOS. Those generated jars are runtime cache files, not
source files or release artifacts.

Finder users can double-click:

```text
Start MCLauncherRevival.command
```

Maintainers can stage an unsigned app bundle for manual testing:

```sh
./package-macos.sh
```

The `.app` output goes under `dist/`, is not signed or notarized, and should not be committed.

## How to use

1. Start `Start MCLR.cmd` or `scripts\run-win.cmd`.
2. Click `Microsoft Login`.
3. Sign in in the browser page that opens.
4. Copy the final registered desktop redirect URL from the browser into the launcher's paste dialog
   (or use `Paste from Clipboard`).
5. With a custom registered client ID, an exact `127.0.0.1` callback can be enabled instead.
6. If your browser changes the final page to
   `https://login.live.com/oauth20_desktop.srf?removed=true`, that means Microsoft scrubbed the
   one-use access token from the address bar after loading the desktop OAuth page.
7. After the profile is fetched, click `Play Online`.
8. For singleplayer without Microsoft auth, type a name and click `Play Offline`.

## Launch behavior

The launcher loads Mojang's official version manifest from:

```text
https://launchermeta.mojang.com/mc/game/version_manifest.json
```

From that manifest it lists classic `old_beta` and `old_alpha` versions from Beta 1.8.x downward.
The bottom bar has an editable `Version:` field, so you can choose a listed version or type a
specific Mojang version id manually.

For the selected version, the launcher finds that version's metadata URL, then downloads the client
jar, libraries, and Windows natives into the normal `.minecraft` folder if they are missing.

Typical locations:

```text
%APPDATA%\.minecraft\versions\<version>\<version>.jar
%APPDATA%\.minecraft\libraries
%APPDATA%\.minecraft\versions\<version>\natives
```

Modern Minecraft access tokens are passed as the legacy session value where possible. Beta 1.7.3
predates modern Yggdrasil/Microsoft authentication, so this is mainly useful for preserving the
authenticated identity and for compatibility with launch arguments. Offline mode is the clear
fallback for singleplayer.

Launch logs are written to:

```text
%APPDATA%\.minecraft\launcher_revive\logs\last-launch.log
```

## Files added

- `src/net/minecraft/MinecraftLauncher.java`
- `src/net/minecraft/ModernAuth.java`
- `src/net/minecraft/BetaLauncher.java`
- `src/net/minecraft/TokenCache.java`
- `src/net/minecraft/AuthProfile.java`
- `src/net/minecraft/Json.java`
- `src/net/minecraft/StatusSink.java`
- `src/net/minecraft/SafeFiles.java`
- `src/net/minecraft/DownloadClient.java`
- `src/net/minecraft/NativeExtractor.java`
- `src/net/minecraft/OAuthCallback.java`
- `src/net/minecraft/MacLaunchConfig.java`
- `tests/net/minecraft/LauncherSelfTest.java`
- `scripts/build-win.cmd`
- `scripts/run-win.cmd`
- `scripts/test-win.cmd`
- `scripts/test-java.sh`
