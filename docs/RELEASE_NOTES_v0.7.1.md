# MCLauncherRevival v0.7.1-alpha Release Notes

v0.7.1-alpha is a focused macOS patch for the unsigned `.app` build published after v0.7.

## Fixed

- Fixed `.app` launches for cached Beta/Alpha versions whose extracted macOS LWJGL natives only
  contained the older `.jnilib` filenames.
- The launcher now repairs the expected `.dylib` compatibility aliases during every launch prep,
  not only when native jars are first extracted.
- The smoke test now checks the macOS native alias repair path.

## Notes

- The macOS app bundle is still unsigned and not notarized.
- The normal release ZIP remains the safest cross-platform package for testers.
- Windows and XP behavior are unchanged by this patch.
