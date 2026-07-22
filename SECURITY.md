# Security Policy

MCLauncherRevival is unofficial and alpha-quality. Authentication-related issues are still treated
seriously, but this hobby project cannot promise professional security response times.

## Account safety

The launcher should never ask users to type a Microsoft password directly into the app.

Expected online login path where available:

1. Browser-based Microsoft OAuth with PKCE.
2. Microsoft's registered desktop redirect by default.
3. Local callback only with a custom registered client ID.
4. Manual redirect-URL paste-back for the desktop handoff.
5. Xbox Live authentication.
6. XSTS authorization.
7. Minecraft services login.
8. Minecraft profile lookup.

Your password should stay in your browser on Microsoft's website. MCLauncherRevival only receives
the tokens Microsoft returns after sign-in approval.

Cached OAuth tokens are stored locally at:

```text
%APPDATA%\.minecraft\launcher_revive\auth.properties
```

Use `Forget Login` in the launcher to remove cached login data and any leftover temporary macOS
game-launch credentials. This removes local copies; it does not revoke an already-issued token at
Microsoft.

OAuth tokens are sensitive. They are not your Microsoft password, but anyone with access to them may
be able to act as your signed-in session until the tokens expire or are revoked.

## Reporting security issues

Please report vulnerabilities through a GitHub issue if the information is safe to discuss publicly.

Do not post access tokens, refresh tokens, authorization codes, Microsoft account details, or
private credentials in public issues.

Good reports include:

- What happened.
- What you expected to happen.
- Windows and Java version.
- Whether the issue involved online login, offline launch, token storage, downloads, or local files.

## Scope

Security-sensitive areas include:

- Password handling.
- OAuth/token storage.
- Download paths and file writes.
- Launch arguments.
- Scripts that run local commands.
