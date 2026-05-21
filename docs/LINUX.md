# Linux Compatibility Notes

Linux support is preliminary. The Java launcher is mostly platform-neutral, but real desktop testing
still needs to be performed on actual Linux distributions.

This repository currently has Windows-tested release behavior and static Linux compatibility checks.
WSL and Docker were not available in the local development environment used for this pass, so this is
not yet a confirmed native Linux field test.

Primary launch log path:

```text
~/.minecraft/launcher_revive/logs/last-launch.log
```

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
- Browser opening first uses Java `Desktop.browse`, then best-effort Linux fallbacks such as
  `xdg-open`, `gio open`, or `sensible-browser` when available.

## Run a release package

After extracting a release ZIP:

```sh
chmod +x scripts/run-linux.sh scripts/build-linux.sh
./scripts/run-linux.sh
```

If `MCLauncherRevival.jar` is already included, `scripts/run-linux.sh` only needs a Java runtime.

If the jar is missing, you may have downloaded GitHub's source-code ZIP instead of the attached
release ZIP. For normal use, download the attached release asset from GitHub Releases. Source ZIPs
are meant for reading/building the code and require a local JDK.

## Build from source on Linux

Install a JDK, then run:

```sh
chmod +x scripts/build-linux.sh
./scripts/build-linux.sh
```

The output is:

```text
MCLauncherRevival.jar
```

## Known Linux limitations

- Native Linux distro testing is still needed.
- Microsoft login depends on the desktop/browser environment and may need manual redirect paste
  fallback.
- Old Minecraft versions may open blank, crash, fail to create an OpenGL context, or fail to load
  LWJGL native libraries depending on the distro, Java version, desktop session, and graphics
  driver.
- Fresh downloads require Java HTTPS/TLS support that works with current Mojang/Minecraft endpoints.
- Headless servers are not a target for the Swing launcher UI.

## Blank window, OpenGL, or LWJGL native failures

If an old Minecraft client opens blank, crashes, or reports an OpenGL/LWJGL native error, check:

```text
~/.minecraft/launcher_revive/logs/last-launch.log
```

Try Java 8 first. Modern Java may run the launcher UI but still fail with old Beta/Alpha clients.
Make sure the machine has a real desktop session and working graphics drivers. Headless Linux is not
supported by the Swing launcher UI.

## Browser did not open

Microsoft login first tries Java desktop browsing, then `xdg-open`, `gio open`, and
`sensible-browser`. If no browser opens, the manual redirect paste flow may still work, but Linux
auth remains experimental.

## Account safety

The launcher should never ask for a raw Microsoft password inside the app. Microsoft sign-in should
use browser/OAuth flow where available, and Offline Play remains available when online login fails.
