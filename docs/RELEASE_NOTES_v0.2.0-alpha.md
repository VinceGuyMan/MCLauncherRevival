# MCLauncherRevival v0.2.0-alpha Release Notes

This alpha release packages the launcher with the Windows 7 dependency/login fixes and the field-test
documentation updates that came out of real Windows 7 testing.

## Status

This is still alpha / experimental software.

- Unofficial project.
- Not affiliated with Mojang, Microsoft, Xbox, or Minecraft.
- Not a replacement for the official Minecraft Launcher.
- Intended for preservation, nostalgia, and learning.
- Use at your own risk.

## Packaging notes

Use the attached GitHub Releases ZIP:

```text
MCLauncherRevival-v0.2.0-alpha.zip
```

Do not use GitHub's green **Code -> Download ZIP** source archive as the runnable package. The source
archive is useful for reading the code, but it may not include the same built jar/package layout.

## Windows 7 Java setup

The dependency setup forces TLS 1.2 before HTTPS downloads and can reuse local Java files before
downloading again.

The setup now:

- Detects an existing `tools\jdk8` install.
- Reuses local `tools\temurin8-jdk.zip` if present.
- Requires a full JDK with `java.exe`, `javac.exe`, and `jar.exe`.
- Prints clearer failure messages if Java setup still fails.

## Microsoft login behavior

Microsoft login opens in the user's default browser instead of the old Internet Explorer COM helper.
On Windows 7, users may still need the redirect URL paste fallback because Microsoft can change the
post-login redirect URL to `removed=true` shortly after login.

The launcher should never ask for a raw Microsoft password inside the app.

## XSTS / Xbox profile troubleshooting

Microsoft login can complete on Windows 7, but the account still needs a valid Xbox profile before
the Xbox Live/XSTS/Minecraft services chain can continue.

If login reaches XSTS authorization and fails with:

```text
XSTS authorization failed (HTTP 401)
XErr: 2148916233
Redirect: https://start.ui.xboxlive.com/
```

try this:

1. Open <https://start.ui.xboxlive.com/>.
2. Finish Xbox profile setup for the Microsoft account.
3. Return to MCLauncherRevival.
4. Click `Forget Login`.
5. Try `Microsoft Login` again.

Offline Play remains available when online authentication fails.

## Run

After extracting the release ZIP, run:

```bat
Start MCLauncherRevival.cmd
```

For Windows XP offline/classic mode:

```bat
Start MCLauncherRevival XP Offline.cmd
```

## Disclaimer

Minecraft is a trademark of Mojang/Microsoft. This project is unofficial and is not affiliated with,
endorsed by, or sponsored by Mojang, Microsoft, Xbox, or Minecraft.
