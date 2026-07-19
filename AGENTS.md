# AGENTS.md

## Project identity

MCLauncherRevival is an unofficial, alpha-quality nostalgia and preservation project that recreates the feel of older Minecraft launchers while using modernized internals where practical.

It is not an official Mojang or Microsoft product, not a replacement for the official Minecraft Launcher, and not a guarantee of compatibility with every historical Minecraft version.

Prioritize, in order:

1. Safe account handling
2. Reliable offline launching
3. Windows 7–11 compatibility
4. Java 7 runtime compatibility where currently supported
5. Clear Windows XP offline/classic behavior
6. Readable, maintainable source
7. Accurate documentation
8. Classic launcher-inspired visual character

Do not sacrifice account safety or compatibility for visual polish.

## Support matrix

### Primary target

- Windows 7 through Windows 11
- Java 8 recommended
- Classic Minecraft Java versions
- Online or offline behavior where supported

### Windows XP

Windows XP support is limited to offline/classic use.

Do not:

- Promise reliable Microsoft login on XP
- Assume XP or Java 7 can connect to modern HTTPS services
- Require fresh online downloads for XP operation
- Remove prepared-file workflows
- Describe XP compatibility as equivalent to modern Windows support

XP users may need versions, libraries, assets, natives, and a compatible Java runtime prepared on a newer machine.

Never weaken TLS or certificate validation to make XP downloads work.

### Linux and macOS

Linux and macOS support remains experimental. A successful build or launcher smoke test does not prove that historical Minecraft clients will launch correctly on those platforms.

## Java compatibility contract

The project currently builds with a JDK 8 toolchain while targeting Java 7 bytecode.

Preserve:

```text
-source 7
-target 7
```

Source under `src/net/minecraft/` must remain compatible with the intended Java 7 runtime path unless the owner explicitly changes the support policy.

Do not introduce:

- Lambdas
- Method references
- Streams
- `var`
- Records
- Modules
- Switch expressions
- Text blocks
- APIs introduced after Java 7 when the code must run on Java 7

Compiling with `-target 7` does not automatically prevent use of newer runtime APIs. Review API availability manually.

Do not raise the source or target level merely to simplify a change.

## Build system

The project intentionally uses direct platform scripts and `javac`/`jar` commands.

Important paths include:

- `scripts/build-win.cmd`
- `scripts/build-linux.sh`
- `build-macos.sh`
- `package-macos.sh`
- `scripts/run-win.cmd`
- `scripts/run-linux.sh`
- `run-macos.sh`

The expected build output is:

```text
MCLauncherRevival.jar
```

with `net.minecraft.MinecraftLauncher` as the main class.

Do not introduce Maven, Gradle, or another build system unless explicitly requested. A replacement build must not displace the existing scripts until all supported workflows are proven equivalent.

## Important project areas

Inspect relevant files before substantial changes:

- `README.md`
- `SECURITY.md`
- `CONTRIBUTING.md`
- `docs/TRUST_AND_SAFETY.md`
- `docs/LINUX.md`
- `docs/MACOS.md`
- `docs/XP_JAVA_SETUP.md`
- `docs/HISTORICAL_THEMES.md`
- `docs/SCREENSHOTS.md`
- `src/net/minecraft/MinecraftLauncher.java`
- `src/net/minecraft/BetaLauncher.java`
- Other source files under `src/net/minecraft/`
- Platform launch, build, and packaging scripts
- `resources/`

Before adding more responsibility to a large Java file, inspect collaborating classes and prefer focused helpers where compatible with the existing architecture.

## Required workflow before editing

Before making changes:

1. Read this file.
2. Check Git status.
3. Do not overwrite, revert, or reformat unrelated work.
4. Determine which platforms are affected.
5. Determine whether Java 7 compatibility is affected.
6. Determine whether authentication, downloads, files, or process launching are affected.
7. Inspect the relevant scripts, source, tests, and documentation.
8. Identify an appropriate validation path.
9. Make the smallest coherent change.

When fixing a platform-specific bug, avoid changing shared behavior unless the shared implementation is genuinely the root cause.

## Authentication safety

Authentication code is security-sensitive.

The launcher must never ask users to type a Microsoft password directly into the application.

Preserve:

- Browser-based Microsoft OAuth
- PKCE where applicable
- OAuth state validation
- Registered redirect behavior
- Explicit callback restrictions
- Safe fallback messaging
- Local token storage
- `Forget Login` behavior
- Clear separation between offline and authenticated profiles

Do not:

- Embed a Microsoft password form
- Log access tokens, refresh tokens, or authorization codes
- Commit OAuth credentials
- Disable state or PKCE validation
- Claim XP can securely support modern authentication
- Send account data to analytics or telemetry

Authentication failures must not silently downgrade an online account into a misleading or unsafe state. Offline mode must remain explicit.

## Token and local-data handling

Treat these as sensitive:

- OAuth tokens and authorization codes
- Account profile data
- Cached login properties
- Custom client IDs
- Redirect configuration
- User `.minecraft` paths
- Launcher logs containing launch arguments

Never commit or expose:

- `auth.properties`
- `.auth.properties`
- `launcher.properties`
- `.env` files
- Credential files
- User `.minecraft` data
- Private logs
- Build-machine paths

When writing token or settings files:

- Use the established launcher data directory
- Prefer safe temporary-file and replace behavior for important writes
- Preserve `Forget Login` deletion behavior
- Never include secrets in exception messages

## Downloads and filesystem safety

Code that downloads, extracts, copies, or deletes files is security-sensitive.

Preserve or add:

- HTTPS on supported platforms
- Clear handling of XP TLS failures
- Connection and read timeouts
- Bounded redirects
- Destination-path validation
- Protection against ZIP path traversal
- Safe parent-directory creation
- Useful partial-download cleanup
- Explicit overwrite behavior
- Clear error messages

Do not disable TLS verification or trust every certificate.

Do not execute downloaded installers or binaries without clear user consent.

## Minecraft-file and licensing rules

Do not commit or redistribute files unless their licensing and redistribution rights are clear.

Never casually commit:

- Minecraft client or server jars
- Mojang or Microsoft launcher assets
- Downloaded Minecraft libraries
- User saves or `.minecraft` folders
- Downloaded JDKs or Java installers
- Native libraries copied from a user installation
- Generated jars or release ZIPs

Use recreated or original project artwork rather than proprietary launcher artwork unless rights have been verified.

## Historical launcher design

Preserve the classic launcher-inspired character where practical.

Historical styles include:

- Beta
- Alpha
- Infdev
- Classic
- Pre-Classic
- Automatic era selection

Do not present recreated themes as exact official historical launcher distributions.

Visual changes should preserve:

- Readability
- Keyboard usability
- Low-end behavior
- Resizable-window behavior
- Existing era distinctions
- Clear online versus offline controls
- Clear status and error information

Do not prioritize pixel-perfect nostalgia over usable account or error messaging.

## Low-end and legacy behavior

`Potato Mode!` and low-memory presets are intentional features.

When adding UI or visual behavior:

- Avoid expensive continuous repaint loops
- Stop timers when views are hidden
- Avoid unnecessary large image allocations
- Preserve compact window sizes
- Test smaller memory presets where practical
- Keep Swing operations on the Event Dispatch Thread
- Keep network and launch work off the Event Dispatch Thread

Do not assume modern graphics drivers or abundant RAM.

## Process launching

Game-launch commands are security- and compatibility-sensitive.

When modifying process construction:

- Preserve argument boundaries
- Use argument arrays instead of concatenated shell commands
- Avoid invoking a shell unless required
- Preserve paths containing spaces
- Protect tokens from logs
- Validate Java executable selection
- Preserve native-library paths
- Preserve working-directory behavior
- Preserve platform-specific flags
- Preserve XP offline restrictions
- Preserve macOS foreground-launch handling

Do not log a full command line when it contains session or access tokens.

A process starting does not prove Minecraft loaded successfully. Preserve early-exit checks and launch-log reporting.

## Platform scripts

Changes to `.cmd`, `.sh`, `.command`, PowerShell, or packaging scripts must be tested or reviewed for their target platform.

For Windows batch files:

- Preserve quoting around paths
- Expect spaces in folder names
- Check `errorlevel`
- Avoid commands unavailable on the stated target Windows version
- Be especially careful with Windows XP compatibility

For shell scripts:

- Use portable syntax where practical
- Preserve executable-bit expectations
- Quote paths and variables
- Stop on meaningful failures
- Avoid assuming GNU-only behavior on macOS

Do not modify every platform script to solve a one-platform bug unless the change is genuinely shared.

## Error messaging

Errors should explain:

- What failed
- Which platform or mode is active
- Whether online or offline mode was involved
- What file or runtime is missing
- What safe corrective action is available
- Where the launcher log can be found

For XP HTTPS failures, explain the prepared-file workflow rather than suggesting insecure TLS workarounds.

For source-code ZIP mistakes, explain that the runnable release asset is required rather than pretending the generated jar exists.

Do not hide exceptions involving authentication, downloads, ZIP extraction, path handling, process startup, or token persistence.

## Testing and validation

Use the established platform build command.

### Windows

```bat
scripts\build-win.cmd
```

### Linux

```sh
./scripts/build-linux.sh
```

### macOS

```sh
./build-macos.sh
```

Run the non-GUI smoke test where possible:

```text
java -jar MCLauncherRevival.jar --smoke-test
```

The smoke test validates launcher startup assumptions and required resources. It does not prove that every Minecraft version launches.

For Java changes, verify:

- Compilation with the intended JDK
- Java 7 source and bytecode compatibility
- No accidental use of newer runtime APIs
- Launcher smoke test
- Relevant UI behavior
- Relevant online or offline launch path
- No token exposure in logs

For platform-specific changes, state which systems were actually tested.

Do not claim Windows XP, Linux, or macOS support based only on testing Windows 11.

## Release packaging

A source-code archive is not the normal runnable package.

Before release-related work:

- Confirm the version
- Build from a clean state
- Verify the jar exists
- Run the smoke test
- Inspect package contents
- Confirm no tokens, local settings, logs, JDKs, installers, or `.minecraft` data leaked into the package
- Verify filenames and documentation
- Distinguish standard and XP-bundled-Java packages
- Include third-party runtime licensing information where required

Do not publish a release, create a tag, or upload artifacts unless explicitly requested.

## Generated and ignored files

Do not commit:

- `build/`, `out/`, `dist/`, `target/`, or `.gradle/`
- `.class` or generated `.jar` files
- `.dmg`, `.pkg`, or `.app` output
- Downloaded JDKs or Java installers
- ZIP packages
- Logs, temporary files, or crash logs
- Auth caches or launcher settings
- IDE metadata

A file being ignored does not make it safe to expose elsewhere.

## Dependency policy

The project is intentionally lightweight.

Before adding a dependency:

- Explain why the Java standard library is insufficient
- Confirm Java 7 runtime compatibility
- Confirm redistribution licensing
- Consider packaging size and old-platform behavior
- Avoid native dependencies where practical
- Avoid telemetry, advertising, tracking, crypto, and unrelated launcher bloat

Do not add a dependency solely to avoid writing a small, understandable utility.

## Documentation

Update documentation during the same task when changing:

- Build requirements
- Java compatibility
- Supported operating systems
- Login behavior
- Token storage
- Offline behavior
- XP setup
- Linux or macOS behavior
- Release packaging
- File locations
- Error-recovery steps
- Historical theme behavior

Documentation must clearly distinguish verified, smoke-tested, experimental, best-effort, and unsupported behavior.

Do not convert an aspiration into a compatibility claim.

## Git rules

Do not commit, push, merge, publish a release, create a tag, or open a pull request unless explicitly requested.

Do not discard unrelated uncommitted work.

When asked to commit:

- Keep the commit focused
- Use a clear message
- Inspect the staged diff
- Confirm no binaries or secrets are included

## Completion standard

Before calling a task complete:

1. Review the complete diff.
2. Confirm no unrelated files changed.
3. Build with the intended toolchain.
4. Run the smoke test where possible.
5. Check Java 7 source and runtime compatibility.
6. Check affected platform scripts.
7. Check account and token safety.
8. Check download and filesystem safety.
9. Remove debug output and temporary files.
10. Update affected documentation.
11. Identify all untested platforms and launch modes.

The final response must report:

- What changed
- Why it changed
- Files affected
- Build commands run
- Smoke-test results
- Platforms tested
- Online and offline paths tested
- Java versions used
- Anything not verified
- Remaining compatibility or security risks
- Safe rollback instructions for substantial changes
