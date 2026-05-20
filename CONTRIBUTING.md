# Contributing

Thanks for helping keep this tiny old launcher weird, useful, and safe.

## Project goals

- Preserve the classic 2011 launcher feeling.
- Keep the UI small, nostalgic, and practical.
- Never collect raw Microsoft passwords.
- Keep Java 7 bytecode compatibility where practical, with Java 8 recommended for modern Windows.
- Prefer lightweight source over heavy dependencies.
- Keep offline singleplayer mode obvious and working.

## Build before submitting changes

On Windows:

```bat
build-win7.cmd
```

For GitHub Actions or other non-interactive shells:

```bat
cmd /c "(echo.)| build-win7.cmd"
```

## Style notes

- Keep Java source compatible with Java 7 bytecode where practical.
- Avoid adding large dependencies unless they are truly necessary.
- Keep new UI controls compact enough for the old launcher footer.
- Do not commit downloaded JDKs, `.minecraft` data, token caches, or generated build folders.

## Good issue details

For launcher bugs, include:

- Windows version.
- Java version shown in the launcher.
- Selected Minecraft version.
- Online or offline launch mode.
- Error text from Launcher Log.
