# Windows XP Java Setup

MCLauncherRevival can run on Windows XP for offline/classic play, but XP needs an old Java runtime.

This guide is for releases that do not include bundled Java.

## Important safety notes

- XP mode is offline/classic only.
- Modern Microsoft login and fresh HTTPS downloads are not reliable on XP.
- Do not download Java from random mirrors or file-sharing sites.
- Old Java runtimes are not secure for general browsing, browser plugins, or production use.
- Use old Java only for this offline/classic launcher scenario.
- Java is third-party software under its own license.

## What to download

Use an official vendor archive when possible.

Recommended for most XP users:

- Java 7 JRE offline installer for Windows x86, named like `jre-7uXX-windows-i586.exe`.

Use this only if you know your XP install is 64-bit:

- Java 7 JRE offline installer for Windows x64, named like `jre-7uXX-windows-x64.exe`.

Only use a JDK if you need build tools:

- Java 7 JDK for Windows x86, named like `jdk-7uXX-windows-i586.exe`.
- Java 7 JDK for Windows x64, named like `jdk-7uXX-windows-x64.exe`.

An XP-compatible Java 8 runtime may also work if you already have one, but Java 7 is the simpler
target for Windows XP.

Official Oracle archive pages:

- Oracle Java Archive: <https://www.oracle.com/java/technologies/downloads/archive/>
- Java SE 7 archive downloads: <https://www.oracle.com/java/technologies/javase/javase7-archive-downloads.html>
- Java SE 8 archive downloads: <https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html>

Oracle archive downloads may require an Oracle account and license acceptance.

## Option A: install Java normally

1. On a modern PC, download the Java installer from an official source.
2. Copy the installer to the Windows XP computer with a USB drive or local network share.
3. Run the installer on Windows XP.
4. Start MCLauncherRevival with:

   ```bat
   Start MCLauncherRevival XP Offline.cmd
   ```

The XP launcher checks `JAVA_HOME` and `java.exe` on `PATH`, so a normal Java install should be
found automatically.

## Option B: use a local launcher runtime folder

If you have a verified redistributable Java runtime folder instead of an installer, place it here:

```text
MCLauncherRevival\
  tools\
    java7\
      bin\
        java.exe
```

The important file is:

```text
tools\java7\bin\java.exe
```

`tools\java7` is the preferred runtime folder for XP release packages. `tools\jdk8` remains a
fallback for users who already have an XP-compatible Java 8 runtime, but new XP packages should use
`tools\java7` when possible.

Then run:

```bat
Start MCLauncherRevival XP Offline.cmd
```

If the launcher uses this folder, it should print:

```text
Using bundled Java runtime: tools\java7
```

## Option C: maintainer-provided installer folder

Some private/test packages may include Java installers under:

```text
tools\java-installers
```

If Java is missing and that folder exists, the XP launcher asks before running one of those
installers. It should not silently install Java.

Public releases should only include Java installers when the maintainer has the right to
redistribute them.

## If Java is still not found

Check these paths in order:

1. `tools\java7\bin\java.exe`
2. `tools\jdk8\bin\java.exe`
3. `%JAVA_HOME%\bin\java.exe`
4. `java.exe` on `PATH`

If none of those work, install Java normally or place an extracted runtime at `tools\java7`.

## Do not use GitHub source ZIPs

For normal use, do not download:

- GitHub's green **Code -> Download ZIP** source archive.
- GitHub's auto-generated tag/source ZIP files.

Use the attached release ZIP from GitHub Releases. Source ZIPs may not include
`MCLauncherRevival.jar` and are meant for reading/building the code.
