# macOS Compatibility Notes

macOS support is still experimental, but the launcher now has first-class macOS helper scripts,
a Finder-friendly `.command` launcher, and an optional unsigned `.app` bundle packager.

Tested locally so far:

- macOS 26.5 on Apple Silicon (`arm64`).
- Launcher build with a local JDK 8 under `tools/jdk8`.
- Swing launcher startup.
- Non-GUI `--smoke-test`.
- Unsigned `.app` bundle creation under `dist/`.

Old Beta/Alpha Minecraft game launch is still experimental on macOS. The launcher may start the
client process, but old LWJGL/OpenGL/native-library behavior can still produce a blank window,
hang, or immediate exit.

Windows remains the primary supported target.

## Java requirement

Java 8 is recommended for old Beta/Alpha Minecraft behavior. A modern JDK may run the launcher UI,
but old game clients can be picky.

Check installed Java versions:

```sh
java -version
/usr/libexec/java_home -V
```

A full JDK is required only when building from source. Building Java 7-compatible bytecode requires
a JDK that still supports that target. Very new JDKs, such as JDK 26, do not.

If your Mac only has a modern JDK and the build reports that Java 7-compatible bytecode is not
supported, install or extract a JDK 8 and set `JAVA_HOME`, or use the project-local helper:

```sh
chmod +x tools/download-temurin8-jdk-macos.sh
./tools/download-temurin8-jdk-macos.sh
```

On Apple Silicon, that helper downloads the x64 Temurin 8 JDK and runs it through Rosetta because
Adoptium does not provide a macOS ARM64 JDK 8 package. It does not install Java system-wide.

## Build

From the repo root:

```sh
chmod +x build-macos.sh run-macos.sh package-macos.sh "Start MCLauncherRevival.command"
./build-macos.sh
```

The output is:

```text
MCLauncherRevival.jar
```

The root script calls `scripts/build-macos.sh`, which:

- Runs from the repo root even when called from another folder.
- Prefers a local `tools/jdk8` runtime.
- Respects valid `JAVA_HOME`.
- Looks for Java 8 through `/usr/libexec/java_home -v 1.8`.
- Falls back to another detected JDK only if needed.
- Fails with an actionable message instead of installing Java automatically.

## Run

From the repo root:

```sh
./run-macos.sh
```

If `MCLauncherRevival.jar` is missing, the run script attempts a local build first. It sets
macOS-friendly launcher process properties:

```text
-Dapple.awt.application.name=MCLauncherRevival
-Xdock:name=MCLauncherRevival
```

It also warns if the detected runtime is newer than Java 8.

## Double-click from Finder

Double-click:

```text
Start MCLauncherRevival.command
```

Depending on how the repo was cloned or unzipped, macOS may remove executable bits. If double-click
does not work, run this once from Terminal:

```sh
chmod +x "Start MCLauncherRevival.command" run-macos.sh build-macos.sh package-macos.sh scripts/run-macos.sh scripts/build-macos.sh
```

## Optional app bundle

Create an unsigned app bundle:

```sh
./package-macos.sh
```

The output is:

```text
dist/MCLauncherRevival.app
```

The bundle layout is:

```text
MCLauncherRevival.app/
  Contents/
    Info.plist
    MacOS/
      MCLauncherRevival
    Resources/
      MCLauncherRevival.jar
      favicon.png
      MCLauncherRevival.icns        if iconutil/sips were available during packaging
```

This bundle is not signed or notarized. Gatekeeper may require right-click > Open or approval from
System Settings > Privacy & Security. Do not describe these alpha builds as notarized unless a
future release is actually signed and notarized.

`dist/` is generated output and should not be committed.

## File locations

Minecraft game files:

```text
~/Library/Application Support/minecraft
```

Launcher settings, auth cache, backups, and logs:

```text
~/Library/Application Support/minecraft/launcher_revive
```

Primary game launch log:

```text
~/Library/Application Support/minecraft/launcher_revive/logs/last-launch.log
```

## Microsoft Login

Microsoft Login opens your default browser for OAuth. The launcher must never ask you to type a raw
Microsoft password into the app.

On macOS, the default public-client auth path uses Microsoft's desktop redirect. After browser
sign-in, the launcher may ask you to copy the final Microsoft redirect URL from the browser address
bar. The paste dialog includes a `Paste from Clipboard` button and watches the clipboard for the
expected redirect URL.

Copy only a URL shaped like:

```text
https://login.live.com/oauth20_desktop.srf?code=...&state=...
```

If Microsoft shows `removed=true` on the page, copy the full address bar URL anyway. That usually
means Microsoft returned to its desktop redirect page after sign-in; it does not mean your password
was given to the launcher.

Do not share redirect URLs, screenshots containing redirect URLs, files from the auth cache, or
OAuth tokens in support chats or GitHub issues. `Forget Login` clears cached local login data.

## Offline mode

Offline Play does not require Microsoft Login. It still needs the selected Minecraft version jar,
JSON metadata, libraries, natives, and sometimes assets to exist locally or be downloadable.

## Apple Silicon notes

The launcher UI and smoke test were checked on Apple Silicon. Old Minecraft/LWJGL natives were not
built for modern Apple Silicon Macs, so actual game launch may require Rosetta/x64 Java and still
may fail because of OpenGL/native compatibility. Treat old-client gameplay on macOS as experimental
until each target version is tested.

## Troubleshooting

Java missing:

- Install Java 8 or set `JAVA_HOME`.
- If building from source, use `tools/download-temurin8-jdk-macos.sh` for a project-local JDK 8.

Java too new:

- The launcher UI may run, but old Minecraft clients may fail.
- Run with Java 8 when testing Beta/Alpha clients.

App will not open:

- For `.command`, restore execute bits with `chmod +x`.
- For `.app`, right-click > Open or approve the unsigned app in System Settings.

Microsoft login weirdness:

- Use the browser address bar only.
- Paste only the final `login.live.com/oauth20_desktop.srf?...` redirect URL.
- Click `Forget Login` and retry after fixing account or family settings.

Version download fails:

- Check Java HTTPS/TLS support.
- Try again on Java 8.
- Verify the selected version exists in Mojang metadata.

Game starts then immediately exits or shows a blank window:

- Open the Launcher Log tab.
- Check:

  ```text
  ~/Library/Application Support/minecraft/launcher_revive/logs/last-launch.log
  ```

- This often points to old LWJGL/OpenGL/native compatibility, not Microsoft login.
