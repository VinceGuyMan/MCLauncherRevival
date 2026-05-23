# macOS Compatibility Notes

macOS support is experimental. The launcher UI may open, but launching old Beta/Alpha Minecraft
clients is not considered supported yet.

This is not yet a notarized `.app` bundle. It is a plain Java jar plus helper shell scripts.

Windows remains the primary supported target.

Primary launch log path:

```text
~/Library/Application Support/minecraft/launcher_revive/logs/last-launch.log
```

## What should work

- The Swing launcher UI should run on macOS with Java installed.
- Launcher settings and token cache use:

  ```text
  ~/Library/Application Support/minecraft/launcher_revive
  ```

- Minecraft game files use:

  ```text
  ~/Library/Application Support/minecraft
  ```

- The launcher selects `osx` native libraries from Mojang version metadata when available.
- Offline mode and UI testing may work even when old game rendering does not.
- Offline Play may work for already-downloaded classic versions if Java/LWJGL/OpenGL/native
  compatibility is satisfied.

## Requirements

- A macOS desktop session.
- Java 8 is the best first test for old Beta/Alpha Minecraft behavior.
- Modern Java may be unreliable for old clients.
- Building Java 7-compatible bytecode requires a JDK that still supports that target. Very new JDKs,
  such as JDK 26, do not.
- A full JDK is required only when building from source.
- On newer macOS versions, Gatekeeper may warn about downloaded scripts or jars from an unsigned
  project.

## Run a release package

After extracting a release ZIP:

```sh
chmod +x scripts/run-macos.sh scripts/build-macos.sh
./scripts/run-macos.sh
```

If `MCLauncherRevival.jar` is already included, `scripts/run-macos.sh` only needs a Java runtime.

If the jar is missing, you may have downloaded GitHub's source-code ZIP instead of the attached
release ZIP. For normal use, download the attached release asset from GitHub Releases. Source ZIPs
are meant for reading/building the code and require a local JDK.

## Build from source on macOS

Install a JDK, then run:

```sh
chmod +x scripts/build-macos.sh
./scripts/build-macos.sh
```

If your Mac only has a modern JDK and the build reports that Java 7-compatible bytecode is not
supported, install a local Temurin 8 JDK into `tools/jdk8`:

```sh
chmod +x tools/download-temurin8-jdk-macos.sh
./tools/download-temurin8-jdk-macos.sh
./scripts/build-macos.sh
```

On Apple Silicon, the helper uses the x64 Temurin 8 JDK through Rosetta because Adoptium does not
provide a macOS ARM64 JDK 8 package. It does not install Java system-wide.

The output is:

```text
MCLauncherRevival.jar
```

## Known macOS limitations

- The launcher UI may open while the game client opens a blank white window titled `Minecraft`.
- Old Beta/Alpha Minecraft clients may fail to render or hang because of LWJGL/OpenGL/Java native
  compatibility.
- Apple Silicon may be more problematic because old native libraries were not built for that
  platform.
- There is no signed or notarized `.app` bundle yet.
- Microsoft login depends on the desktop/browser environment and may need manual redirect paste
  fallback.
- Old Minecraft versions may be sensitive to Java and LWJGL native-library combinations.
- Fresh downloads require Java HTTPS/TLS support that works with current Mojang/Minecraft endpoints.

## Blank Minecraft window

If Minecraft opens as a blank white window, the launcher may have started the client process
successfully, but the old client may be stuck in LWJGL/OpenGL/native initialization.

Check the Launcher Log tab and this file:

```text
~/Library/Application Support/minecraft/launcher_revive/logs/last-launch.log
```

The launcher status may still correctly say:

```text
Minecraft started. Launch log: <path>
```

That means the process was started; it does not guarantee that the old Minecraft client rendered
successfully on macOS.

## Browser did not open

Microsoft login first tries Java desktop browsing, then a macOS `open` fallback. If no browser opens,
the manual redirect paste flow may still work, but macOS auth remains experimental.

## Account safety

The launcher should never ask for a raw Microsoft password inside the app. Microsoft sign-in should
use browser/OAuth flow where available, and Offline Play remains available when online login fails.
