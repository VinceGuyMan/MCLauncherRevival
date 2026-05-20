# XP Version Setup

Windows XP offline/classic mode still needs Minecraft version files to already exist locally.

Offline mode means no Microsoft login. It does not mean MCLauncherRevival can run without the
version jar, JSON metadata, libraries, natives, and sometimes assets.

## Why XP can fail with `handshake_failure`

Windows XP and Java 7 often cannot negotiate with modern HTTPS services. Mojang/Minecraft metadata,
client jars, libraries, assets, and version manifests are served from HTTPS endpoints that may fail
on XP with errors such as:

- `handshake_failure`
- `Received fatal alert`
- `PKIX`
- `unable to find valid certification path`

When that happens, the safest fix is to prepare the files on a newer PC and copy them to XP.

## Why only `b1.7.3` may appear

If XP cannot load Mojang's modern version manifest, MCLauncherRevival falls back to `b1.7.3` so the
launcher still opens.

You can type a version manually in the version box, but it will only launch if the matching files
already exist locally.

## Loose jars are not enough

This is not enough:

```text
.minecraft\versions\b1.7.3.jar
```

MCLauncherRevival expects the Mojang launcher-style layout:

```text
.minecraft\versions\b1.7.3\b1.7.3.jar
.minecraft\versions\b1.7.3\b1.7.3.json
.minecraft\libraries\
.minecraft\assets\
```

The `.json` file tells the launcher which main class, arguments, libraries, natives, and assets the
version needs.

## Preferred method: prepare on Windows 7 or newer

1. On Windows 7 or newer, run MCLauncherRevival.
2. Select the classic version you want, for example `b1.7.3`.
3. Click `Play Offline` once and let the launcher prepare/download files.
4. Close Minecraft.
5. Copy these folders from the newer PC:

   ```text
   %APPDATA%\.minecraft\versions
   %APPDATA%\.minecraft\libraries
   %APPDATA%\.minecraft\assets
   ```

6. Paste them on Windows XP at:

   ```text
   C:\Documents and Settings\<User>\Application Data\.minecraft\versions
   C:\Documents and Settings\<User>\Application Data\.minecraft\libraries
   C:\Documents and Settings\<User>\Application Data\.minecraft\assets
   ```

7. Start MCLauncherRevival on XP:

   ```bat
   Start MCLauncherRevival XP Offline.cmd
   ```

8. Select or type the prepared version and click `Play Offline`.

## Optional manual jar method with MCVersions.net

The preferred method is still copying the full prepared `.minecraft` folders from Windows 7 or
newer.

MCVersions.net can be useful as an optional convenience for locating old Minecraft client jars that
are served from Mojang, but it is not an official MCLauncherRevival project source.

1. Go to:

   ```text
   https://mcversions.net/
   ```

2. Search for the desired version, for example:

   ```text
   b1.7.3
   ```

3. Download the client jar only if you own Minecraft Java Edition or otherwise have the right to use
   the files.

4. Create this folder on XP:

   ```text
   C:\Documents and Settings\<User>\Application Data\.minecraft\versions\b1.7.3\
   ```

5. Place the jar here:

   ```text
   C:\Documents and Settings\<User>\Application Data\.minecraft\versions\b1.7.3\b1.7.3.jar
   ```

6. Still provide the matching files/folders:

   ```text
   b1.7.3.json
   libraries folder
   assets folder
   ```

MCVersions.net can help locate a client jar, but it does not replace the full prepared `.minecraft`
version folder. If the JSON metadata or libraries are missing, the launcher may still try to
download files, which often fails on XP.

## Legal reminder

Do not bundle Minecraft client jars, libraries, assets, or Mojang game files with MCLauncherRevival.

Use only Minecraft files you own or otherwise have the right to use. MCLauncherRevival is
unofficial, alpha-quality software and is not affiliated with, endorsed by, or sponsored by Mojang,
Microsoft, Xbox, or Minecraft.
