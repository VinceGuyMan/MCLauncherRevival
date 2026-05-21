# Windows 7 Build and Run Guide

MCLauncherRevival supports Windows XP for offline/classic play and Windows 7 SP1 through modern
Windows for the full modern-auth launcher experience. This guide covers Windows 7 and newer, where
Java 8 is recommended. The jar is compiled as Java 7 bytecode so XP offline/classic mode remains
supported.

## Run

Double-click:

```bat
Setup MCLauncherRevival.cmd
```

Then choose the Windows 7-11 normal launcher path. You can also run `Start MCLauncherRevival.cmd`
or `run-win7.cmd` directly.

If Java is missing, the script offers to download Eclipse Temurin 8 JDK into:

```text
tools\jdk8
```

This is portable and local to the project folder.

## Release ZIP vs source ZIP

For normal use, download the attached GitHub Releases ZIP. Do not use GitHub's green
**Code -> Download ZIP** source archive as the runnable package.

Source ZIPs are useful for reading or building the code, but they may not include
`MCLauncherRevival.jar`. If you use a source ZIP, Windows 7+ may try to build locally and require a
JDK.

## Build

Double-click:

```bat
build-win7.cmd
```

The build output is:

```text
MCLauncherRevival.jar
```

## Manual Java install

If the automatic download fails, install a Java 8 JDK manually and make sure these are on `PATH`:

```text
java.exe
javac.exe
jar.exe
```

Then run:

```bat
build-win7.cmd
run-win7.cmd
```

## Troubleshooting

### Microsoft login reaches XSTS but fails with XErr 2148916233

This usually means the Microsoft account can sign in, but does not have an Xbox profile set up yet.
MCLauncherRevival still has to pass through Xbox Live/XSTS before Minecraft services login can
continue.

If you see:

```text
XSTS authorization failed (HTTP 401)
XErr: 2148916233
Redirect: https://start.ui.xboxlive.com/
```

try this:

1. Open <https://start.ui.xboxlive.com/>.
2. Finish Xbox profile setup for the Microsoft account.
3. Return to MCLauncherRevival.
4. Click `Forget Login`.
5. Try `Microsoft Login` again.

Offline Play remains available if online authentication still fails.

### Browser redirect changes to removed=true

On Windows 7, Microsoft login may require the redirect URL paste fallback. After login, Microsoft
can change the redirect URL to `removed=true`, so copy the full redirect URL as soon as the blank
redirect page appears and paste it back into the launcher.

### Redownload Version safety

`Redownload Version` deletes only the selected folder under:

```text
%APPDATA%\.minecraft\versions\<selected-version>
```

It does not delete saves, auth tokens, libraries, assets, or unrelated folders. The launcher rejects
empty or path-like version names before deleting anything.

## Common notes

- Java 8 is recommended for old Beta/Alpha Minecraft.
- Newer Java may run the launcher but old Minecraft/LWJGL can be picky.
- The launcher writes game files to the normal `%APPDATA%\.minecraft` folder.
- Offline mode remains available even if Microsoft login fails.

