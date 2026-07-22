# Contributing

MCLauncherRevival is a hobby nostalgia project. Contributions are welcome when they keep the project
focused, readable, and safe.

## 📋 Guidelines

- Keep pull requests focused and reviewable.
- Use clear commit messages.
- Preserve the classic launcher feel where practical.
- Keep Java source compatible with the current build target unless there is a clear reason to change it.
- Do not add official branding claims or imply Mojang/Microsoft approval.
- Do not add malware, account-stealing code, spam, ads, tracking, telemetry, crypto, or unrelated launcher bloat.
- Do not commit downloaded JDKs, build output, `.minecraft` data, token caches, local settings, or secrets.

## 🔨 Building

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

The expected build output is:

```text
MCLauncherRevival.jar
```

Run the dependency-free launcher self-tests after building:

```bat
scripts\test-win.cmd
```

```sh
./scripts/test-java.sh
```

## 🐛 Useful issue details

For launcher bugs, please include:

- **Windows version** (e.g., Windows 11, Windows XP)
- **Java version** shown in the launcher
- **Selected Minecraft version**
- **Launch mode**: Online or offline
- **Error text** from Launcher Log

## 💡 Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/your-username/MCLauncherRevival.git`
3. Create a branch: `git checkout -b feature/your-feature-name`
4. Make your changes
5. Test your changes
6. Submit a pull request
