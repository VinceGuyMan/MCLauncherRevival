# macOS Compatibility Notes

macOS support is preliminary. The Java launcher has platform handling for macOS paths and native
libraries, but real macOS testing still needs to be performed on Apple hardware or a macOS VM.

This is not yet a notarized `.app` bundle. It is a plain Java jar plus helper shell scripts.

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
- Offline Play should work for already-downloaded classic versions if Java/LWJGL compatibility is
  satisfied.

## Requirements

- A macOS desktop session.
- Java 8 is recommended for old Beta/Alpha Minecraft behavior.
- A full JDK is required only when building from source.
- On newer macOS versions, Gatekeeper may warn about downloaded scripts or jars from an unsigned
  project.

## Run a release package

After extracting a release ZIP:

```sh
chmod +x run-macos.sh build-macos.sh
./run-macos.sh
```

If `MCLauncherRevival.jar` is already included, `run-macos.sh` only needs a Java runtime.

## Build from source on macOS

Install a JDK, then run:

```sh
chmod +x build-macos.sh
./build-macos.sh
```

The output is:

```text
MCLauncherRevival.jar
```

## Known macOS limitations

- Native macOS testing is still needed.
- There is no signed or notarized `.app` bundle yet.
- Microsoft login depends on the desktop/browser environment and may need manual redirect paste
  fallback.
- Old Minecraft versions may be sensitive to Java and LWJGL native-library combinations.
- Fresh downloads require Java HTTPS/TLS support that works with current Mojang/Minecraft endpoints.

## Account safety

The launcher should never ask for a raw Microsoft password inside the app. Microsoft sign-in should
use browser/OAuth flow where available, and Offline Play remains available when online login fails.
