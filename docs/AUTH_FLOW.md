# Authentication and Launch Flow

MCLauncherRevival keeps the old launcher look, but avoids the unsafe legacy username/password login.

## Modern online flow

```mermaid
flowchart LR
    User["Player clicks Microsoft Login"]
    Browser["Browser OAuth sign-in"]
    MS["Microsoft OAuth authorization code"]
    MSToken["Microsoft OAuth access/refresh token"]
    XBL["Xbox Live authentication"]
    XSTS["XSTS authorization"]
    MC["Minecraft services login"]
    Profile["Minecraft profile lookup"]
    Launch["Launch selected classic client"]

    User --> Browser
    Browser --> MS
    MS --> MSToken
    MSToken --> XBL
    XBL --> XSTS
    XSTS --> MC
    MC --> Profile
    Profile --> Launch
```

## Offline/classic flow

```mermaid
flowchart LR
    Name["Offline profile name"]
    Version["Selected classic version"]
    Cache["Local .minecraft files"]
    Launch["Launch offline singleplayer"]

    Name --> Launch
    Version --> Cache
    Cache --> Launch
```

## Current browser/OAuth behavior

The preferred alpha flow is browser-based Microsoft authorization-code login with `offline_access`.
When Microsoft returns a refresh token, MCLauncherRevival stores it locally so future sessions can
refresh without asking the user to sign in every time.

The launcher still accepts a legacy pasted `access_token` redirect as a compatibility fallback, but
the normal flow should be an authorization code.

## Token cache

OAuth tokens are cached locally so the user does not need to sign in every time:

```text
%APPDATA%\.minecraft\launcher_revive\auth.properties
```

The launcher does not ask for or store raw Microsoft passwords.

Use `Forget Login` to clear cached OAuth tokens.

## Xbox profile requirement

The Microsoft account must have an Xbox profile before the Xbox Live/XSTS step can succeed. If XSTS
fails with `XErr: 2148916233`, open <https://start.ui.xboxlive.com/>, finish Xbox profile setup,
then use `Forget Login` and try `Microsoft Login` again.

## Windows XP note

Windows XP is supported for offline/classic play. Modern Microsoft login and fresh HTTPS downloads
are best-effort on XP because the operating system, browser stack, Java TLS support, and root
certificates are often too old for current Microsoft/Minecraft services.
