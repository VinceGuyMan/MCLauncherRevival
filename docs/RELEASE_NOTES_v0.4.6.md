# MCLauncherRevival v0.4.6-alpha

This alpha keeps the launcher behavior from v0.4.5 and focuses on repository/release package cleanup.

## Repository layout cleanup

- Moved build, run, and packaging helpers into `scripts/` so the GitHub root is easier to scan.
- Kept the double-click Windows entrypoints in the repo root for normal users:
  - `Setup MCLR.cmd`
  - `Start MCLR.cmd`
  - `Start MCLR XP.cmd`
- Updated root launchers so they call the moved helper scripts safely.
- Updated release packaging so runnable ZIPs include helper scripts under `scripts/`.
- Updated documentation and GitHub Actions paths for the new script location.

## Compatibility notes

- Windows 7-11 remains the main full launcher path.
- Windows XP remains offline/classic focused.
- macOS and Linux remain experimental.
- The launcher still should never ask for a raw Microsoft password inside the app.

## Download

Use the attached release asset:

```text
MCLauncherRevival-v0.4.6-alpha.zip
```

Do not use GitHub's green **Code -> Download ZIP** button or the auto-generated source/tag ZIP for normal play.
