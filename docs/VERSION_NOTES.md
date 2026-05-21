# Historical Version Notes

MCLauncherRevival keeps its in-launcher Update Notes and Patch Notes Mode offline-friendly. The launcher does not scrape websites at runtime just to display historical notes.

## How notes are matched

1. Exact version ID first, for example `b1.7.3`.
2. Family fallback second, for example `b1.7.x`.
3. Unknown fallback last.

When an exact note is not available, the launcher says so clearly:

```text
Exact historical notes for this build were not found. Showing the nearest verified era summary instead.
```

## Launcher-visible version range

The launcher reads Mojang's official version manifest and loads legacy `old_beta` / `old_alpha` entries starting around Beta 1.8.x and downward. A current manifest pass showed these IDs:

```text
b1.8.1, b1.8, b1.7.3, b1.7.2, b1.7, b1.6.6, b1.6.5, b1.6.4,
b1.6.3, b1.6.2, b1.6.1, b1.6, b1.5_01, b1.5, b1.4_01, b1.4,
b1.3_01, b1.3b, b1.2_02, b1.2_01, b1.2, b1.1_02, b1.1_01,
b1.0.2, b1.0_01, b1.0, a1.2.6, a1.2.5, a1.2.4_01,
a1.2.3_04, a1.2.3_02, a1.2.3_01, a1.2.3, a1.2.2b, a1.2.2a,
a1.2.1_01, a1.2.1, a1.2.0_02, a1.2.0_01, a1.2.0, a1.1.2_01,
a1.1.2, a1.1.0, a1.0.17_04, a1.0.17_02, a1.0.16, a1.0.15,
a1.0.14, a1.0.11, a1.0.5_01, a1.0.4, inf-20100618, c0.30_01c,
c0.0.13a, c0.0.13a_03, c0.0.11a, rd-161348, rd-160052,
rd-20090515, rd-132328, rd-132211
```

Exact notes are present for the higher-confidence and more historically important entries first. Other builds fall back to clearly labeled family notes until exact details are verified.

## Sources used

The first static notes pass uses public historical references:

- Mojang's official version manifest: https://launchermeta.mojang.com/mc/game/version_manifest.json
- Minecraft Wiki Java Edition version history pages: https://minecraft.wiki/
- Minecraft Timeline as a secondary orientation aid: https://minecraft-timeline.github.io/

The notes are summaries, not full copied patch-note dumps. They are intentionally short so they fit inside the old launcher-style news panel.

## Writing rules for future notes

- Prefer exact version pages over broad era pages.
- Do not invent features or dates.
- If documentation is sparse, say that the exact compact entry is sparse.
- Separate verified facts from launcher commentary.
- Keep wording short, historical, and lightly nostalgic.
- Include source links.
- Do not add runtime web scraping.
- Keep the code Java 7/Java 8 compatible and dependency-free.

## Where to edit

- Static note data lives in `src/net/minecraft/VersionNoteData.java`.
- HTML rendering lives in `src/net/minecraft/VersionNotes.java`.

A good note includes:

- title
- version ID
- release date when known
- family or era
- short summary
- added / changed / fixed / removed lists when known
- known quirks
- why it matters
- launcher commentary
- source links

## Known limits

Some very early Alpha, Classic, and Pre-Classic launcher-visible files have archival or recompile caveats. For those builds, notes should stay cautious and source-led rather than pretending every file maps cleanly to a polished historical release.
