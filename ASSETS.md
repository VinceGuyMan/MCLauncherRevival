# Asset Provenance

The MIT license covers original MCLauncherRevival code and project-owned artwork. It does not grant
rights to third-party names, trademarks, game files, or assets whose origin has not been verified.

## Inventory

| Files | Recorded origin | Redistribution status |
| --- | --- | --- |
| `resources/net/minecraft/themes/*.png` | Recreated specifically for MCLauncherRevival | Project-owned; covered by this repository's license |
| `docs/screenshots/*.png` except `backdrop.png` | Captures made from this project | Project-generated captures; captured third-party marks and inherited/unverified resource elements retain their underlying status |
| `resources/net/minecraft/macos-game-launcher.c` | Original MCLauncherRevival compatibility helper | Project-owned; covered by this repository's license |
| `resources/net/minecraft/favicon.png` | Added as MCLauncherRevival icon artwork | Editable source/authorship is not recorded in the repository; exclude from the MIT grant until it is documented |
| `docs/screenshots/backdrop.png` | Composite presentation image added for MCLauncherRevival; includes recognizable Windows and Minecraft UI/marks | Component sources and permissions are not documented in the repository; exclude from the MIT grant and review or replace before treating redistribution rights as verified |
| `resources/net/minecraft/logo.png`, `dirt.png`, `Block.png`, `StevePlaceholder.jpg`, `scrolls.png`, and `cobalt.png` | Carried forward from the recovered historical launcher resource set | Origin and license are not established in this repository; excluded from the project's MIT grant and should be reviewed or replaced before redistribution rights are considered verified |

No Minecraft client jars, libraries, native binaries, account data, or downloaded game assets should
be committed or included in a project release. The launcher downloads a user's game files at runtime
from permitted HTTPS endpoints or uses files the user has prepared locally.

## Adding assets

Every new visual or binary asset should have a recorded author/source, license or permission, and a
short purpose statement. Prefer original project artwork. Do not add extracted official launcher or
game artwork merely to improve visual fidelity.

If an existing asset cannot be traced, replace it with an original equivalent or obtain and record
permission. Do not assume that historical availability or inclusion in an old archive grants reuse
rights.
