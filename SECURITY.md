# Security Policy

## Supported versions

The current maintained branch is the GitHub `main` branch after the modernization work.

## Authentication safety

MCLauncherRevive must not ask for or store raw Microsoft passwords.

Expected login flow:

1. Microsoft browser OAuth
2. Xbox Live authentication
3. XSTS authorization
4. Minecraft services login
5. Minecraft profile lookup

Cached OAuth tokens are stored locally at:

```text
%APPDATA%\.minecraft\launcher_revive\auth.properties
```

Use `Forget Login` in the launcher to delete cached tokens.

## Reporting issues

Please open a GitHub issue for:

- Login flow failures.
- Accidental token/password exposure.
- Unsafe storage behavior.
- Download or launch paths that write outside `.minecraft`.

Do not paste access tokens, refresh tokens, authorization codes, or Microsoft account details into public issues.
