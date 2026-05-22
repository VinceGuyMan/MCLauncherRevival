# Contributing

MCLauncherRevival is a hobby nostalgia project. Contributions are welcome when they keep the project
focused, readable, and safe.

## Guidelines

- Keep pull requests focused and reviewable.
- Use clear commit messages.
- Preserve the classic launcher feel where practical.
- Keep Java source compatible with the current build target unless there is a clear reason to change
  it.
- Do not add official branding claims or imply Mojang/Microsoft approval.
- Do not add malware, account-stealing code, spam, ads, tracking, telemetry, crypto, or unrelated
  launcher bloat.
- Do not commit downloaded JDKs, build output, `.minecraft` data, token caches, local settings, or
  secrets.

## Building

On Windows:

```bat
build-win.cmd
```

The expected build output is:

```text
MCLauncherRevival.jar
```

## Useful issue details

For launcher bugs, include:

- Windows version.
- Java version shown in the launcher.
- Selected Minecraft version.
- Online or offline launch mode.
- Error text from Launcher Log.
