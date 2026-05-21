# Historical Launcher Themes

MCLauncherRevival v0.5.0 adds recreated launcher-era presentations. The goal is to capture the
shape and mood of historical launcher windows while keeping the project safe, readable, and
unofficial.

## Reference approach

Reference screenshots are used as design notes only. They inform proportions, panel placement,
tab names, sidebar density, and the general feel of each era.

The project does not redistribute extracted Mojang/Microsoft launcher art unless redistribution
rights are verified. Theme textures in this repository are recreated project-owned pixel assets.

## Current styles

| Style | Intent |
| --- | --- |
| Beta | News-heavy layout with tabs, right links, footer controls, and animated splash text. |
| Alpha | Simpler launcher presentation with chunkier panels and early survival-era tone. |
| Infdev | Sparse experimental layout with prototype-style notes. |
| Classic | Minimal early-web style with login/version-panel inspiration, but no raw password login. |
| Pre-Classic | Stripped-down prototype layout with rougher panels and early-build notes. |

## Auto mapping

| Selected version | Resolved style |
| --- | --- |
| `b*` | Beta |
| `a*` | Alpha |
| `inf-*` | Infdev |
| `c0.*` | Classic |
| `rd-*` | Pre-Classic |

## Shared backend

Every style keeps the same backend and safety model:

- Browser/OAuth Microsoft login where available.
- No raw Microsoft password prompts inside the launcher.
- Offline singleplayer fallback.
- Version selection and local version scanning.
- Launcher Log and Profile Editor diagnostics.
- XP offline/classic boundaries.

## Future refinement

Future releases can improve fidelity by recreating more era-specific spacing, control placement,
and typography while continuing to avoid bundled proprietary launcher assets.
