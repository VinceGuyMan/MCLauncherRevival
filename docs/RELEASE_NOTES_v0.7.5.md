# MCLauncherRevival v0.7.5-alpha Release Notes

v0.7.5-alpha is a security, reliability, and release-quality update for the classic launcher
revival. It keeps Java 7 runtime compatibility while using a JDK 8 build toolchain.

## Security and account handling

- Restricts launcher downloads and redirects to HTTPS, bounds response/file sizes, and validates
  download destinations.
- Validates the complete Microsoft OAuth callback origin and path and avoids exposing raw provider
  responses in errors.
- Writes tokens, settings, and transient macOS launch configuration atomically with private local
  permissions where supported.
- Deletes the one-use macOS session configuration as soon as the helper opens it. `Forget Login`
  also clears cached tokens, interrupted writes, and leftover launch credentials.
- Verifies Adoptium's published SHA-256 before extracting portable Temurin 8 downloads.

## Reliability and testing

- Hardens native-library extraction against traversal and oversized archives.
- Fixes readiness checks for incomplete version folders and captures Swing launch selections on
  the event thread.
- Adds 33 dependency-free launcher self-test checks and runs them on Windows, macOS, and Linux CI.
- Keeps launcher and test classes at Java bytecode major version 51 for the Java 7 runtime path.

## Packaging and documentation

- Makes the standard ZIP self-contained with buildable source, resources, and self-tests, preserves
  Unix permissions, and runs the extracted package's tests in Linux CI.
- Adds an asset provenance inventory and clarifies which inherited assets still require permission
  documentation or replacement.
- Reduces the README backdrop from 14.5 MB to 3.0 MB while retaining a 1920-pixel presentation copy.
- Corrects stale login, build-script, setup, and release-package documentation.

## Known limits

- The project remains alpha-quality and unofficial.
- Windows XP remains offline/classic only.
- Linux and macOS historical game launch remains experimental.
- The macOS app is unsigned and not notarized.
- Assets marked unverified in `ASSETS.md` remain outside the project's MIT grant.
