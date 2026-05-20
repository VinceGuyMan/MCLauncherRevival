# Linux Compatibility Notes

Linux support is preliminary. The Java launcher is mostly platform-neutral, but real desktop testing
still needs to be performed on actual Linux distributions.

This repository currently has Windows-tested release behavior and static Linux compatibility checks.
WSL and Docker were not available in the local development environment used for this pass, so this is
not yet a confirmed native Linux field test.

## What should work

- The Swing launcher UI should run on a desktop Linux system with Java installed.
- Launcher settings and token cache use:

  ```text
  ~/.minecraft/launcher_revive
  ```

- Minecraft game files use:

  ```text
  ~/.minecraft
  ```

- The launcher selects Linux native libraries from Mojang version metadata when available.
- Offline Play should work for already-downloaded classic versions if Java/LWJGL compatibility is
  satisfied.

## Requirements

- A desktop Linux session with X11 or Wayland available.
- Java 8 is recommended for old Beta/Alpha Minecraft behavior.
- A full JDK is required only when building from source.
- `xdg-open` is not required by the launcher; Java `Desktop.browse` is used for browser links when
  the desktop supports it.

## Run a release package

After extracting a release ZIP:

```sh
chmod +x run-linux.sh build-linux.sh
./run-linux.sh
```

If `MCLauncherRevival.jar` is already included, `run-linux.sh` only needs a Java runtime.

## Build from source on Linux

Install a JDK, then run:

```sh
chmod +x build-linux.sh
./build-linux.sh
```

The output is:

```text
MCLauncherRevival.jar
```

## Known Linux limitations

- Native Linux distro testing is still needed.
- Microsoft login depends on the desktop/browser environment and may need manual redirect paste
  fallback.
- Old Minecraft versions may be sensitive to Java and LWJGL native-library combinations.
- Fresh downloads require Java HTTPS/TLS support that works with current Mojang/Minecraft endpoints.
- Headless servers are not a target for the Swing launcher UI.

## Account safety

The launcher should never ask for a raw Microsoft password inside the app. Microsoft sign-in should
use browser/OAuth flow where available, and Offline Play remains available when online login fails.
