package net.minecraft;

final class VersionNotes {
    private VersionNotes() {
    }

    static String page(String version) {
        return page(version, false);
    }

    static String page(String version, boolean patchMode) {
        return page(version, patchMode, false);
    }

    static String page(String version, boolean patchMode, boolean compact) {
        String splash = SplashText.forKey(clean(version) + ":" + patchMode);
        Note note = noteFor(version);
        return "<html><body text='#e8e8e8' link='#aaaaff' vlink='#aaaaff' style='font-family:Verdana,Arial,sans-serif;font-size:11px;margin:24px;background-color:transparent'>"
                + "<table width='100%' cellpadding='0' cellspacing='0'><tr>"
                + "<td><font size='+3'><b>Minecraft News</b></font></td>"
                + "<td align='right'><font color='#ffff55'><b>" + escape(splash) + "</b></font></td>"
                + "</tr></table>"
                + "<br><br>"
                + "<p><a href='" + note.learnMore + "'><b>" + escape(note.title) + "</b></a></p>"
                + "<p><font color='#b8b8ff'><b>Selected version:</b> " + escape(clean(version)) + "</font><br>"
                + "<font color='#999999'>Timeline family: " + escape(note.family) + "</font></p>"
                + (compact ? compactSection(note, version, patchMode) : (patchMode ? patchSection(note, version) : updateSection(note, version)))
                + (patchMode ? "" : "<br><p><a href='https://www.minecraft.net/'><b>MCLauncherRevival Alpha Released</b></a></p>"
                + "<p>The old launcher window learned modern Microsoft auth while keeping the big dark update notes, tiny bottom bar, and blocky nostalgia intact.</p>"
                + "<p>+ Browser-based Microsoft login<br>"
                + "+ Local token caching with Forget Login<br>"
                + "+ Offline singleplayer<br>"
                + "+ Selectable classic versions from Beta 1.8.x down through early alpha/classic builds</p>")
                + "<hr color='#333333'>"
                + "<p><font color='#888888'>Version summaries are compact, launcher-friendly notes based on Minecraft Timeline data. "
                + "Read more at <a href='https://minecraft-timeline.github.io/'>minecraft-timeline.github.io</a>. "
                + "MCLauncherRevival Alpha.</font></p>"
                + "</body></html>";
    }

    private static String compactSection(Note note, String version, boolean patchMode) {
        return "<table width='100%' cellpadding='8' cellspacing='0' bgcolor='#0d0d0d' style='border:1px solid #444444'><tr><td>"
                + "<b>" + escape(patchMode ? "Compact patch notes" : "Compact update notes") + " - " + escape(clean(version)) + "</b><br><br>"
                + escape(note.summary) + "<br><br>"
                + "<b>Highlights:</b><br>" + bulletLines(note.mainFeatures)
                + "</td></tr></table>";
    }

    private static String updateSection(Note note, String version) {
        return "<p><font color='#999999'>Posted in the style of the old launcher update feed.</font></p>"
                + "<p>" + escape(note.summary) + "</p>"
                + "<p><b>Why players remember this era:</b><br>" + sentenceLines(note.mainFeatures) + "</p>"
                + "<p><b>Launcher note:</b> " + escape(note.launcherNote) + "</p>"
                + "<br><p><font color='#999999'>For today's selected build, <b>" + escape(clean(version)) + "</b>, the launcher has pulled together a small era summary instead of a giant wiki dump. "
                + "Think of this as the old news page telling you why this version matters before you press Play.</font></p>";
    }

    private static String patchSection(Note note, String version) {
        return "<table width='100%' cellpadding='8' cellspacing='0' bgcolor='#0d0d0d' style='border:1px solid #444444'><tr><td>"
                + "<font size='+1'><b>Patch Notes - " + escape(clean(version)) + "</b></font><br><br>"
                + "<font color='#bbbbbb'>Compact launcher notes for the " + escape(note.family) + " era.</font><br><br>"
                + "<b>Added:</b><br>" + bulletLines(note.mainFeatures) + "<br><br>"
                + "<b>Changed:</b><br>" + bulletLines(note.minorFeatures) + "<br><br>"
                + "<b>Launcher commentary:</b><br>+ " + escape(note.launcherNote) + "<br>"
                + "+ Notes are intentionally compact so the launcher still feels like an update panel, not a wiki dump.<br>"
                + "+ Use the title link above for fuller historical reading."
                + "</td></tr></table>";
    }

    private static Note noteFor(String rawVersion) {
        String version = clean(rawVersion);
        if (version.startsWith("b1.8")) {
            return new Note("Adventure Update 1", "Beta 1.8", "Endermen, hunger, creative mode, villages, and strongholds pushed Minecraft toward adventure-game territory.",
                    "Endermen|Hunger|Creative Mode|Villages|Strongholds",
                    "Melons|Cave Spiders|Swamps|More world structures|Far Lands changes",
                    "Beta 1.8.x is the newest era this launcher is meant to cover.",
                    "https://minecraft.wiki/w/Java_Edition_Beta_1.8");
        }
        if (version.startsWith("b1.7")) {
            return new Note("The Piston Era", "Beta 1.7", "Pistons turned redstone contraptions from clever wiring into moving machinery.",
                    "Pistons|Sticky Pistons|Shears",
                    "More redstone contraptions|Better block interaction|Classic pre-hunger survival",
                    "A great default if you want the famous golden-age Beta feel.",
                    "https://minecraft.wiki/w/Java_Edition_Beta_1.7");
        }
        if (version.startsWith("b1.6")) {
            return new Note("Maps & Trapdoors", "Beta 1.6", "Exploration got easier with maps, while bases got more compact with trapdoors.",
                    "Maps|Trapdoors|Tall Grass",
                    "Dead Bushes|Many bug fixes|Classic survival polish",
                    "Some Beta 1.6 builds are charmingly rough; if one acts up, try b1.6.6.",
                    "https://minecraft.wiki/w/Java_Edition_Beta_1.6");
        }
        if (version.startsWith("b1.5")) {
            return new Note("Achievements & Weather", "Beta 1.5", "The world started feeling moodier with rain, snow, thunderstorms, achievements, and powered rails.",
                    "Achievements|Powered Rails|Detector Rails|Rain and Thunderstorms",
                    "More sapling types|Weather ambience|Minecart infrastructure",
                    "Bring an umbrella. Or don't. It is Beta.",
                    "https://minecraft.wiki/w/Java_Edition_Beta_1.5");
        }
        if (version.startsWith("b1.4")) {
            return new Note("Wolves & Cookies", "Beta 1.4", "Wolves arrived, cookies appeared, and the Minecraft logo entered a more recognizable era.",
                    "Wolves|Cookies|New Logo",
                    "Tamed companions|More cozy survival flavor|A very 2011 update",
                    "If a wolf sits forever, that is not a launcher bug. That is loyalty.",
                    "https://minecraft.wiki/w/Java_Edition_Beta_1.4");
        }
        if (version.startsWith("b1.3")) {
            return new Note("Beds & Repeaters", "Beta 1.3", "Sleeping through the night and delaying redstone signals changed daily survival routines.",
                    "Beds|Redstone Repeaters",
                    "Graphics settings|New save format work|Smoother early-game rhythm",
                    "Beds are the original 'skip this problem' button.",
                    "https://minecraft.wiki/w/Java_Edition_Beta_1.3");
        }
        if (version.startsWith("b1.2")) {
            return new Note("Note Blocks & Cake", "Beta 1.2", "This era added musical blocks, cake, dispensers, lapis lazuli, dyes, and several familiar decorative ingredients.",
                    "Note Blocks|Cake|Dispensers|Lapis Lazuli",
                    "Squids|Sugar|Sandstone|Wool Dyes",
                    "Strong dessert energy. Also the notes go plonk.",
                    "https://minecraft.wiki/w/Java_Edition_Beta_1.2");
        }
        if (version.startsWith("b1.1")) {
            return new Note("Leaf Decay", "Beta 1.1", "Trees became less eternal, and the world started cleaning up after chopped logs more sensibly.",
                    "Better Leaf Decay",
                    "Small fixes|Early Beta polish|Still very lightweight",
                    "A tiny update, but your floating tree ghosts may disagree.",
                    "https://minecraft.wiki/w/Java_Edition_Beta_1.1");
        }
        if (version.startsWith("b1.0")) {
            return new Note("Beta Begins", "Beta 1.0", "Minecraft entered Beta with throwable eggs and the strange magic of early survival getting more settled.",
                    "Throwable Eggs",
                    "Beta branding|Small gameplay polish|The start of the public Beta age",
                    "Welcome to Beta: still janky, now historic.",
                    "https://minecraft.wiki/w/Java_Edition_Beta_1.0");
        }
        if (version.startsWith("a1.2")) {
            return new Note("Halloween Update", "Alpha 1.2", "The Nether arrived, bringing portals, pumpkins, biomes, fishing, and a big shift in Minecraft's sense of place.",
                    "The Nether|Nether Portals|Pumpkins",
                    "Biomes|Fishing|A much stranger world",
                    "If the sky turns red, congratulations: the launcher did its job.",
                    "https://minecraft.wiki/w/Halloween_Update");
        }
        if (version.startsWith("a1.1")) {
            return new Note("Compasses", "Alpha 1.1", "Compasses helped players find their way home, and lava buckets became furnace fuel.",
                    "Compasses",
                    "Lava bucket furnace fuel|Navigation improvements|Late Alpha polish",
                    "You still might get lost, but now you can do it with confidence.",
                    "https://minecraft.wiki/w/Java_Edition_Alpha_v1.1.0");
        }
        if (version.startsWith("a1.0")) {
            return new Note("Redstone Arrives", "Alpha 1.0", "Redstone dust, torches, levers, buttons, pressure plates, and iron doors gave players the first real wiring toolkit.",
                    "Redstone Dust|Redstone Torches|Levers|Buttons|Pressure Plates|Iron Doors",
                    "Redstone Ore|Simple circuits|The beginning of impossible door machines",
                    "This is where many beautiful contraptions and many terrible tutorials began.",
                    "https://minecraft.wiki/w/Java_Edition_Alpha_v1.0.1");
        }
        if (version.startsWith("inf-")) {
            return new Note("Minecraft Infdev", "Infdev", "Infinite worlds, dungeons, rails, doors, buckets, arrows, and golden apples made Minecraft feel much larger.",
                    "Infinite Worlds|Dungeons|Rails",
                    "Arrows|Doors|Buckets|Golden Apples",
                    "Infdev builds are old enough to creak. Launch with curiosity.",
                    "https://minecraft.wiki/w/Minecraft_Infdev");
        }
        if (version.startsWith("c0.")) {
            return new Note("Classic Creative", "Classic", "Classic builds focused on building, block placement, multiplayer experiments, flowers, glass, wool, and the earliest creative sandbox feel.",
                    "Creative Building|Multiplayer Tests|Glass|Wool",
                    "Flowers|Commands|Mushrooms|Survival Test overlap",
                    "These builds are ancient. If one launches, treat it like finding a fossil that still opens.",
                    "https://minecraft.wiki/w/Java_Edition_Classic");
        }
        if (version.startsWith("rd-")) {
            return new Note("Pre-Classic", "Pre-Classic", "The earliest public/prototype era began with stone, grass, dirt, planks, cobblestone, and basic block placement.",
                    "Grass Blocks|Stone|Dirt|Cobblestone|Planks",
                    "Basic world generation|Block placing and breaking|Very early physics",
                    "This is practically cave-painting Minecraft.",
                    "https://minecraft.wiki/w/Java_Edition_pre-Classic");
        }
        return new Note("Classic Minecraft", "Unknown old version", "This is an old Minecraft build from Mojang's legacy manifest. Specific notes were not found, but the launcher can still try to prepare and run it.",
                "Official Mojang metadata|Legacy client jar|Offline and online launch arguments",
                "Version-specific quirks|Old Java/LWJGL behavior|Probably some goblin energy",
                "If this version misbehaves, try a nearby listed Beta or Alpha build.",
                "https://minecraft-timeline.github.io/");
    }

    private static String bulletLines(String pipeList) {
        String[] parts = pipeList.split("\\|");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            out.append("+ ").append(escape(parts[i]));
            if (i + 1 < parts.length) {
                out.append("<br>");
            }
        }
        return out.toString();
    }

    private static String sentenceLines(String pipeList) {
        String[] parts = pipeList.split("\\|");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            out.append("- ").append(escape(parts[i]));
            if (i + 1 < parts.length) {
                out.append("<br>");
            }
        }
        return out.toString();
    }

    private static String clean(String value) {
        if (value == null || value.trim().length() == 0) {
            return BetaLauncher.DEFAULT_VERSION;
        }
        return value.trim();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static final class Note {
        final String title;
        final String family;
        final String summary;
        final String mainFeatures;
        final String minorFeatures;
        final String launcherNote;
        final String learnMore;

        Note(String title, String family, String summary, String mainFeatures, String minorFeatures, String launcherNote, String learnMore) {
            this.title = title;
            this.family = family;
            this.summary = summary;
            this.mainFeatures = mainFeatures;
            this.minorFeatures = minorFeatures;
            this.launcherNote = launcherNote;
            this.learnMore = learnMore;
        }
    }
}

