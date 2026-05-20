# Uploading to GitHub

The Codex GitHub connector could see `VinceGuyMan/MCLauncherRevival`, but GitHub blocked writes with:

```text
403 Resource not accessible by integration
```

That means the connector does not currently have permission to write to this repository.

## Option 1: One-click helper

Double-click:

```bat
push-to-github.cmd
```

This initializes this clean `github-upload` folder as a git repo and pushes it to:

```text
https://github.com/VinceGuyMan/MCLauncherRevival.git
```

You may need to sign in through Git Credential Manager.

## Option 2: GitHub web upload

Open:

```text
https://github.com/VinceGuyMan/MCLauncherRevival
```

Upload the contents of this `github-upload` folder.

Do not upload the parent project folder directly. The parent folder contains local build/dependency files that should stay off GitHub.

## Included here

- Source code
- Docs
- Build/run scripts
- GitHub Actions workflow
- Required launcher images/resources
- Java dependency downloader script

## Not included

- Local downloaded JDK
- `tools/temurin8-jdk.zip`
- `build/`
- `.svn/`
- Generated local caches
- Auth/token files
