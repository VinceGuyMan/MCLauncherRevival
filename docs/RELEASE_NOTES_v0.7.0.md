# MCLauncherRevival v0.7.0-alpha Release Notes

v0.7.0-alpha is the macOS usability and launchability candidate. It keeps Windows as the primary
target, but adds a much more complete Mac build/run/package path and fixes the tested old-client
startup issues found during local macOS field testing.

## macOS quick start

From a release ZIP or source checkout on macOS:

```sh
chmod +x "Start MCLauncherRevival.command" run-macos.sh build-macos.sh package-macos.sh
./run-macos.sh
```

You can also double-click `Start MCLauncherRevival.command` from Finder.

To create the unsigned app bundle:

```sh
./package-macos.sh
```

The bundle is staged at:

```text
dist/MCLauncherRevival.app
```

The app is unsigned and not notarized. Gatekeeper may require right-click > Open or approval from
System Settings > Privacy & Security.

## What changed for Mac

- Added root macOS build/run wrappers, a Finder `.command` launcher, and unsigned `.app` packaging.
- Added a macOS GitHub Actions smoke/package job and a safe `--smoke-test` mode.
- Improved Swing accessibility, keyboard behavior, menu items, and macOS font handling.
- Improved Microsoft Login on macOS with a larger redirect paste dialog, clipboard paste, clipboard
  watching, retry feedback, and no broken code-login button.
- Removed the extra macOS compatibility warning popup before Play.
- Fixed the clipped Microsoft redirect paste dialog on macOS.
- Improved Play status/error handling so immediate old-client exits are surfaced instead of looking
  like an endless hang.
- Launched tested old clients through a locally built foreground app helper so modern macOS shows a
  real Minecraft window instead of treating the child Java process as background-only.
- Disabled the old JInput default controller plugin for macOS game launches to avoid broad Input
  Monitoring prompts for normal play.
- Fixed the blue/cyan old-client color swap seen on modern macOS by generating local corrected
  runtime copies of the user's own downloaded Minecraft/LWJGL jars.
- Updated the app/favicon image.

## Tested locally

- macOS 26.5 on Apple Silicon with Rosetta/x64 Java 8.
- `./build-macos.sh`
- `java -jar MCLauncherRevival.jar --smoke-test`
- `./package-macos.sh`
- Offline b1.6.6 and b1.7.3 title-screen launch through the macOS foreground helper.
- b1.6.6 and b1.7.3 color correction on the title screen.

## Known limits

- macOS support is still alpha and old Minecraft/LWJGL behavior may vary by Mac, Java runtime, and
  selected version.
- The app bundle is unsigned and not notarized.
- Building from source and first-time old-client launch can require Apple's Command Line Tools so
  the small foreground helper can be compiled locally.
- Old clients still use legacy audio/resource paths; `last-launch.log` may show old
  `s3.amazonaws.com/MinecraftResources` failures even when the title screen opens.
- Windows XP remains offline/classic only and still needs prepared version files plus compatible
  Java and graphics drivers.

## Safety notes

- The launcher still uses browser-based Microsoft OAuth and must never ask for a raw Microsoft
  password inside the app.
- The macOS color-correction jars are generated locally under
  `~/Library/Application Support/minecraft/launcher_revive/runtime/color-fix` from the user's own
  downloaded game files. They are not committed, packaged, or redistributed as project assets.
- Do not publish generated jars, `.minecraft` data, auth caches, tokens, local settings, or bundled
  Java runtimes unless their redistribution terms are explicitly verified.
