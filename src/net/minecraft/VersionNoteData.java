package net.minecraft;

import java.util.HashMap;
import java.util.Map;

final class VersionNoteData {
    private static final String MANIFEST = "Mojang manifest=https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final Map<String, Note> EXACT = new HashMap<String, Note>();
    private static final Map<String, Note> FAMILY = new HashMap<String, Note>();

    static final class Note {
        final String versionId;
        final String title;
        final String releaseDate;
        final String family;
        final String shortSummary;
        final String added;
        final String changed;
        final String fixed;
        final String removed;
        final String knownQuirks;
        final String whyItMatters;
        final String launcherCommentary;
        final String sources;
        final boolean exact;

        Note(String versionId, String title, String releaseDate, String family,
             String shortSummary, String added, String changed, String fixed,
             String removed, String knownQuirks, String whyItMatters,
             String launcherCommentary, String sources, boolean exact) {
            this.versionId = safe(versionId);
            this.title = safe(title);
            this.releaseDate = safe(releaseDate);
            this.family = safe(family);
            this.shortSummary = safe(shortSummary);
            this.added = safe(added);
            this.changed = safe(changed);
            this.fixed = safe(fixed);
            this.removed = safe(removed);
            this.knownQuirks = safe(knownQuirks);
            this.whyItMatters = safe(whyItMatters);
            this.launcherCommentary = safe(launcherCommentary);
            this.sources = safe(sources);
            this.exact = exact;
        }

        String[] addedItems() {
            return split(added);
        }

        String[] changedItems() {
            return split(changed);
        }

        String[] fixedItems() {
            return split(fixed);
        }

        String[] removedItems() {
            return split(removed);
        }

        String[] quirkItems() {
            return split(knownQuirks);
        }

        String[] sourceItems() {
            return split(sources);
        }

        String primaryUrl() {
            String[] items = sourceItems();
            for (int i = 0; i < items.length; i++) {
                int eq = items[i].indexOf('=');
                if (eq >= 0 && eq + 1 < items[i].length()) {
                    return items[i].substring(eq + 1);
                }
            }
            return "https://minecraft.wiki/";
        }
    }

    static {
        exact(n("b1.8.1", "Adventure Update polish", "September 15, 2011", "Beta 1.8",
                "Beta 1.8.1 was a focused follow-up to the first Adventure Update release. It kept the new hunger, villages, strongholds, ravines, sprinting, and Creative mode direction while fixing several launch-day problems.",
                "",
                "Mineshaft support beams now use fences instead of wooden planks.",
                "Fixed a crash when shift-clicking into a full chest or inventory.|Fixed a dispenser-related crash from the Beta 1.8 release.",
                "",
                "Still belongs to the Adventure Update era, so it does not play like the older no-hunger Beta builds.",
                "This is the last Beta version in the official launcher-visible legacy list and a useful marker for the end of the pre-release Beta period.",
                "A small patch, but an important one if you want the Adventure Update without the sharpest launch-day edges.",
                wiki("Java_Edition_Beta_1.8.1")));

        exact(n("b1.8", "The Adventure Update begins", "September 14, 2011", "Beta 1.8",
                "Beta 1.8 was the first Adventure Update release and changed Minecraft's survival rhythm heavily. Hunger, sprinting, villages, strongholds, ravines, mineshafts, Endermen, and Creative mode all pushed the game toward the structure later players recognize.",
                "Hunger bar.|Sprinting.|Creative mode.|Endermen.|Villages, strongholds, abandoned mineshafts, and ravines.|New food behavior and bow charging.",
                "Combat and survival pacing changed around hunger and sprinting.|World generation gained large adventure structures.",
                "Many fixes were folded into the following b1.8.1 patch.",
                "",
                "A major direction change from classic Beta survival. Players looking for the old no-hunger loop usually choose b1.7.3 or earlier.",
                "This is where Minecraft starts feeling much closer to modern survival, even though it is still unmistakably Beta.",
                "The launcher treats this as the big historical hinge: old Beta behind you, Adventure-era Minecraft ahead.",
                wiki("Java_Edition_Beta_1.8")));

        exact(n("b1.7.3", "Classic piston-era Beta", "July 8, 2011", "Beta 1.7",
                "Beta 1.7.3 is remembered as one of the last major pre-Adventure builds. It preserves the classic no-hunger survival loop while keeping pistons, shears, and other late-Beta improvements.",
                "",
                "Focused on stabilizing the piston update rather than adding new gameplay systems.",
                "Fixed piston block duplication issues.|Fixed redstone torch duplication.|Fixed piston and sign crash cases.|Fixed several powered rail, ice, door particle, painting, and multiplayer sign issues.",
                "",
                "Last public Beta build before the Adventure Update pre-release era. Also one of the last builds with the old dirt-background title screen style.",
                "For many players, this is the golden-age Beta target: pistons are in, hunger is not.",
                "A sturdy default if you want old survival with enough redstone toys to make trouble.",
                wiki("Java_Edition_Beta_1.7.3")));

        exact(n("b1.7.2", "Piston crash cleanup", "July 1, 2011", "Beta 1.7",
                "Beta 1.7.2 was a quick repair build after the piston update. It addressed crash and disappearance problems that could appear in newly updated worlds.",
                "",
                "",
                "Fixed crashed worlds related to the piston update.|Fixed disappearing pistons.|Fixed multiplayer piston crashes.",
                "",
                "The 1.7 patch line moved quickly; Beta 1.7.1 was skipped in public release naming.",
                "It matters mostly as a stabilizing step between the original piston release and the better-known b1.7.3.",
                "A tiny patch note with a very Beta job: stop the new machine blocks from eating the world.",
                wiki("Java_Edition_Beta_1.7.2")));

        exact(n("b1.7", "The Piston Update", "June 30, 2011", "Beta 1.7",
                "Beta 1.7 introduced pistons and sticky pistons, giving redstone builders real moving machinery. Shears also arrived, changing how players collected wool and leaves.",
                "Pistons.|Sticky Pistons.|Shears.",
                "Wool and leaf collection changed because shears became the intended tool.",
                "",
                "",
                "Some early piston behavior could crash or corrupt worlds, including dangerous cases involving block 36. Later 1.7.x builds are safer choices for normal play.",
                "This is the start of moving-block redstone. A huge creative leap, even if the first build had rough edges.",
                "The update where redstone stopped only switching things and started pushing things around.",
                wiki("Java_Edition_Beta_1.7")));

        exact(n("b1.6.6", "Map update cleanup", "May 31, 2011", "Beta 1.6",
                "Beta 1.6.6 was a late maintenance build for the map update cycle. It adjusted performance options and glowstone behavior while continuing to smooth over the fast-moving 1.6 patch line.",
                "Video settings gained a Performance option.",
                "Glowstone material, drops, and crafting behavior were adjusted.",
                "Fixed additional issues from the Beta 1.6 map-update sequence.",
                "",
                "The Beta 1.6 line had many quick bug-fix releases. Exact issue lists are best checked against the source page when debugging a specific bug.",
                "A more settled map-update build before the piston era arrived.",
                "Not flashy, but useful if you want maps without jumping all the way to pistons.",
                wiki("Java_Edition_Beta_1.6.6")));

        exact(n("b1.6", "The Map Update", "May 26, 2011", "Beta 1.6",
                "Beta 1.6 is the map update: it added craftable maps and several exploration-era blocks while also kicking off a very busy bug-fix sequence.",
                "Maps.|Trapdoors.|Tall grass and dead bushes.",
                "Exploration gained an in-game mapping tool.",
                "The first release had enough issues that several b1.6.x fixes followed rapidly.",
                "",
                "If a specific b1.6.x bug matters, prefer a later patch such as b1.6.6 and check the source page.",
                "Maps made worlds feel more like places you could chart instead of just wander through.",
                "A cartographer's update, with just enough Beta chaos to keep the patch notes busy.",
                wiki("Java_Edition_Beta_1.6")));

        exact(n("b1.5_01", "Weather-era bug fixes", "April 20, 2011", "Beta 1.5",
                "Beta 1.5_01 fixed several rough edges from the weather, achievement, and powered-rail update. It is a compact maintenance release, but a useful one for this era.",
                "",
                "Snow layers became replaceable, making block placement on snowy terrain less awkward.|Wolf spawn rates were reduced.",
                "Fixed rain passing through translucent blocks.|Fixed pumpkins rendering backward when worn.|Fixed rail-related crashes and ladder spacing issues.|Fixed a furnace output exploit and helped recover some broken saves.",
                "",
                "Exact fixes are fairly specific; check the source page if you are hunting one particular Beta 1.5 bug.",
                "It smooths out one of the first versions where Minecraft begins tracking achievements and world weather together.",
                "A small umbrella for a very rainy update cycle.",
                wiki("Java_Edition_Beta_1.5_01")));

        exact(n("b1.5", "Weather, achievements, and rails", "April 19, 2011", "Beta 1.5",
                "Beta 1.5 added weather, statistics, achievements, and powered rail systems. It made worlds feel more alive while giving minecart travel a more practical redstone backbone.",
                "Weather, including rain and snow.|Statistics and achievements.|Powered rails and detector rails.|Saplings for the different tree types.|Cobwebs.",
                "Minecart travel became easier to automate.|The game began tracking more player progress through statistics and achievements.",
                "",
                "",
                "The first release had several bugs fixed by b1.5_01 the next day.",
                "Weather and achievements are both major atmosphere changes, while powered rails reshape practical travel.",
                "Rain outside, achievement popups inside, minecarts finally getting a proper shove.",
                wiki("Java_Edition_Beta_1.5")));

        exact(n("b1.4_01", "Locked chest cleanup", "April 5, 2011", "Beta 1.4",
                "Beta 1.4_01 cleaned up the April Fools locked chest behavior and fixed bed respawn handling. It is mostly a tidy-up patch for the wolf update era.",
                "",
                "Locked chests no longer generated as an April Fools feature and decayed like leaves.",
                "Fixed bed respawn point behavior.",
                "Partially removed locked chests from normal play.",
                "Locked chests remain a historical oddity from the 2011 April Fools period.",
                "The serious part of this patch is bed respawning; the weird part is Mojang cleaning up a joke block.",
                "A little less prank, a little more sleep working correctly.",
                wiki("Java_Edition_Beta_1.4_01")));

        exact(n("b1.4", "Wolves arrive", "March 31, 2011", "Beta 1.4",
                "Beta 1.4 added wolves, cookies, and the short-lived locked chest April Fools feature.",
                "Wolves.|Cookies.|Locked chests as an April Fools feature.",
                "Tamed wolves added a companion system to survival play.",
                "The release was reuploaded to remove debug code.",
                "",
                "Locked chests were a joke feature and were cleaned up afterward.",
                "Wolves are the lasting headline: Minecraft survival gained a loyal, occasionally cliff-diving friend.",
                "The dog update, plus one very strange chest.",
                wiki("Java_Edition_Beta_1.4")));

        exact(n("b1.3_01", "McRegion bug-fix follow-up", "February 23, 2011", "Beta 1.3",
                "Beta 1.3_01 was a fast bug-fix release after the McRegion and bed update. It repaired notable crashes and compatibility issues from the previous day.",
                "",
                "Removed a Java 1.6 dependency.",
                "Fixed a macOS singleplayer crash.|Fixed an item duplication glitch.|Improved client/server compatibility with the b1.3 line.",
                "",
                "The underlying save-format shift still belongs to Beta 1.3 itself; this patch mainly makes that transition safer.",
                "It matters because b1.3 changed how worlds were stored, and the next patch made the landing less rough.",
                "A one-day safety net for a big storage-format jump.",
                wiki("Java_Edition_Beta_1.3_01")));

        exact(n("b1.3b", "Beta 1.3 reupload", "February 22, 2011", "Beta 1.3",
                "b1.3b is the launcher-visible reupload of Beta 1.3 after debug code and crash issues were addressed. Its main historical identity is still the Beta 1.3 bed, repeater, slab, and McRegion update.",
                "See b1.3 for the main feature additions.",
                "Reupload/build cleanup around the original Beta 1.3 release.",
                "Addressed crash/debug-code issues associated with the first b1.3 upload.",
                "",
                "This is best understood as a reuploaded b1.3 build, not a fully separate feature update.",
                "It preserves the same historical shift while being the version the launcher can actually fetch.",
                "Same big b1.3 mood, slightly less debug baggage.",
                wiki("Java_Edition_Beta_1.3")));

        exact(n("b1.3", "Beds, repeaters, and McRegion", "February 22, 2011", "Beta 1.3",
                "Beta 1.3 added beds, redstone repeaters, more slab types, and a new splash screen while changing world saves to the McRegion format.",
                "Beds.|Redstone repeaters.|Additional slab types.|New splash screen.",
                "World saves changed to the McRegion format.",
                "A reupload and b1.3_01 followed to correct issues from the first release.",
                "",
                "Moving worlds across this era can expose save-format quirks. Keep backups when testing old saves.",
                "Beds changed survival pacing; repeaters changed redstone; McRegion changed how worlds lived on disk.",
                "A cozy nap on top of a serious file-format renovation.",
                wiki("Java_Edition_Beta_1.3")));

        exact(n("b1.2_02", "Skin and launcher packaging fix", "January 21, 2011", "Beta 1.2",
                "Beta 1.2_02 was a compact fix build in the lapis, dyes, squid, and note-block era. It changed skin download handling and corrected launcher packaging details.",
                "Shift+F2 large TGA screenshot test.",
                "Skin downloads moved to a different location.|Lapis lazuli ore drops and bedrock-level availability were adjusted.",
                "Fixed a launcher copy missing META-INF.",
                "",
                "Mostly a technical and resource-handling patch, not a content headline.",
                "It matters for launcher compatibility history more than for moment-to-moment survival.",
                "One of those little builds where the launcher itself is part of the story.",
                wiki("Java_Edition_Beta_1.2_02")));

        exact(n("b1.2", "Dyes, squid, note blocks, and lapis", "January 13, 2011", "Beta 1.2",
                "Beta 1.2 was the first Minecraft version released in 2011 and added several systems that became everyday Minecraft: dyes, note blocks, dispensers, lapis lazuli, sandstone, sugar, bone meal, and squid.",
                "Dyes and bone meal.|Note blocks.|Dispensers.|Squid.|Lapis lazuli ore and blocks.|Sandstone and sugar.|Charcoal.",
                "Crafting and decoration expanded sharply through dye colors and new materials.",
                "",
                "",
                "A known Nether-portal-while-riding issue could crash or damage player data in this era.",
                "This is a huge early-Beta content burst: more color, more sound, more redstone utility, and Minecraft's first aquatic mob.",
                "The world got bluer, louder, and a little stranger underwater.",
                wiki("Java_Edition_Beta_1.2")));

        exact(n("b1.1_02", "Empty-hand chest fix", "December 22, 2010", "Beta 1.1",
                "Beta 1.1_02 was a small fix release from the final days of 2010. Its most visible documented fix was making chests open correctly even when the player's hand was empty.",
                "",
                "",
                "Fixed chests not opening when the player's hand was empty.",
                "",
                "A very narrow patch; the source page is the best reference for the exact historical note.",
                "Small fixes like this are why the official manifest carries multiple near-identical early Beta builds.",
                "A tiny chest fix, preserved because history likes receipts.",
                wiki("Java_Edition_Beta_1.1_02")));

        exact(n("b1.0.2", "Early Beta macOS fix", "December 21, 2010", "Beta 1.0",
                "Beta 1.0.2 was an early Beta hotfix. The historical notes point especially at startup problems on macOS and a reupload because one build could not start.",
                "",
                "",
                "Fixed macOS startup behavior.|Corrected a pre-reupload build that could not start.",
                "",
                "Beta 1.0.1 as a dotted release did not exist; the nearby official ID is b1.0_01.",
                "It shows how quickly the first Beta builds were being repaired after release.",
                "Early Beta: even launching the game could be part of the patch notes.",
                wiki("Java_Edition_Beta_1.0.2")));

        exact(n("b1.0", "Minecraft enters Beta", "December 20, 2010", "Beta 1.0",
                "Beta 1.0 marks Minecraft's move out of Alpha. It added working server-side inventory support and introduced cosmetic account-era details such as capes and the famous deadmau5 ears support.",
                "Working server-side inventory.|Cape/cloak support.|deadmau5 ears support.|New splash texts.",
                "The project entered the Beta development label and a more settled public-testing phase.",
                "",
                "",
                "Very early Beta still carries Alpha-era roughness in networking, saves, and compatibility.",
                "This is the label change that says Minecraft is becoming more stable, even if it is still far from modern release Minecraft.",
                "The launcher says Beta, but the dirt still remembers Alpha.",
                wiki("Java_Edition_Beta_1.0")));

        exact(n("a1.2.6", "Final Alpha build", "December 3, 2010", "Alpha 1.2",
                "Alpha v1.2.6 was the final Alpha version. It capped the Halloween Update era with lakes, rare lava pools, command behavior, and several practical bug fixes before Minecraft moved into Beta.",
                "Small water lakes.|Rare lava pools.|/kill damage behavior.",
                "World generation gained small surface water and rare lava pools.",
                "Fixed item-use behavior with chests.|Fixed boat break drops.|Fixed duplicated entities and vehicle crash cases.",
                "",
                "Modern launcher/session handling can show odd default names if not launched with the expected old arguments.",
                "It is the end of Alpha: Nether-era survival just before the Beta label arrives.",
                "The last Alpha stop before the sign changes to Beta.",
                wiki("Java_Edition_Alpha_v1.2.6")));

        exact(n("a1.2.0", "The Halloween Update", "October 30, 2010", "Alpha 1.2",
                "Alpha v1.2.0 was the Halloween Update. It brought the Nether, biomes, fishing, new blocks, and new hostile mobs into Minecraft's Alpha era.",
                "The Nether.|Biomes.|Fishing.|New Nether blocks and mobs.|New terrain-generation behavior.",
                "World identity changed dramatically through biomes and Nether travel.",
                "Several issues were repaired through the following a1.2.0_01 and a1.2.0_02 builds.",
                "",
                "The official launcher's a1.2.0 entry is historically tied to later reupload behavior, so exact file identity can be confusing.",
                "This is one of Alpha's biggest leaps: Minecraft gained another dimension and a much stronger sense of place.",
                "Portals, pumpkins, fishing, and a whole lot of lava under the floorboards.",
                wiki("Java_Edition_Alpha_v1.2.0")));

        exact(n("a1.1.2_01", "Alpha sound-resource redirect", "September 23, 2010", "Alpha 1.1",
                "Alpha v1.1.2_01 changed resource downloading to Amazon S3 and is notable because sounds can still work in modern launcher setups. It is also among the last versions before the Alpha 1.2 terrain changes.",
                "",
                "Resource downloads moved from Minecraft.net to Amazon S3.",
                "",
                "",
                "Known as the oldest version where sounds can work in the modern launcher. It is also tied to the last old terrain generator, bright grass look, and several old-world oddities.",
                "This build matters for preservation because audio/resource behavior is part of whether old versions feel alive today.",
                "A quiet technical change that makes old Alpha a little less quiet.",
                wiki("Java_Edition_Alpha_v1.1.2_01")));

        exact(n("a1.1.2", "Alpha display and sound fixes", "September 18, 2010", "Alpha 1.1",
                "Alpha v1.1.2 fixed sound and graphical issues from the previous Alpha builds. It also removed debug gamma and contrast controls that could cause a gray-screen problem.",
                "",
                "Removed debug gamma and contrast controls.",
                "Fixed sound-related problems.|Fixed graphical/gray-screen behavior from the debug controls.",
                "",
                "Sound handling in very old Alpha builds can still be awkward in modern launch environments.",
                "It is a practical stability stop in the last stretch before the Halloween Update.",
                "Less gray screen, more actual Minecraft.",
                wiki("Java_Edition_Alpha_v1.1.2")));

        exact(n("a1.1.0", "Last Seecret Friday", "September 10, 2010", "Alpha 1.1",
                "Alpha v1.1.0 was the ninth and last Seecret Friday update. It added shared client-server inventory behavior and the compass.",
                "Compass.|Shared client-server inventory behavior.",
                "Inventory behavior moved closer to the later survival model.",
                "",
                "",
                "The launcher-visible file is associated with a reupload/recompiled build, which is common in this era.",
                "The compass is a small item with a big preservation feeling: it points you home in worlds that suddenly feel larger.",
                "The last Seecret Friday left players with a needle pointing spawnward.",
                wiki("Java_Edition_Alpha_v1.1.0")));

        exact(n("a1.0.17_04", "Late Seecret Friday fixes", "August 23, 2010", "Alpha 1.0",
                "Alpha v1.0.17_04 was a maintenance build in the Seecret Friday period. Historical notes point to fixes for players getting stuck in some cases.",
                "",
                "",
                "Fixed some cases where players could become stuck.",
                "",
                "This build is sometimes mentioned near old Herobrine-stream trivia, but the launcher note sticks to verified gameplay history.",
                "Small fixes from this period help preserve the feel of Alpha without leaning on myths.",
                "A practical fix from an era that collected legends anyway.",
                wiki("Java_Edition_Alpha_v1.0.17_04")));

        exact(n("a1.0.16", "Server operators return", "August 12, 2010", "Alpha 1.0",
                "Alpha v1.0.16 restored command usability on servers and added server operators. It also included unused files related to additional music discs.",
                "Server operators.|Unused files for additional music discs.",
                "Server command administration became usable again.",
                "",
                "",
                "Multiplayer in this era is historically important but much rougher than modern Minecraft servers.",
                "It matters because old survival multiplayer was still becoming manageable for server hosts.",
                "A small admin update from when running a server felt like holding it together with string.",
                wiki("Java_Edition_Alpha_v1.0.16")));

        exact(n("a1.0.11", "Slimes and clay", "July 23, 2010", "Alpha 1.0",
                "Alpha v1.0.11 was Seecret Friday 6. It added clay, reeds, paper, books, bricks, slimeballs, and slimes.",
                "Clay.|Reeds/sugar cane.|Paper and books.|Bricks.|Slimeballs and slimes.",
                "World resources and crafting expanded with materials that would become long-term staples.",
                "",
                "",
                "Slime behavior and spawning in old versions can differ sharply from modern expectations.",
                "This is an early example of Minecraft adding both practical materials and a strange new creature at once.",
                "Books, bricks, and bouncing green trouble.",
                wiki("Java_Edition_Alpha_v1.0.11")));

        exact(n("inf-20100618", "Seecret Friday minecarts", "June 18, 2010", "Infdev",
                "Infdev 20100618 was Seecret Friday 1 and is the only Infdev version exposed by the official modern manifest in this launcher range. It added rails and minecarts to an experimental infinite-world Minecraft.",
                "Rails.|Minecarts.",
                "Transportation became possible inside the still-primitive Infdev environment.",
                "",
                "",
                "Infdev builds are experimental and can behave very differently from later Alpha and Beta versions.",
                "Minecarts are a major survival-machine idea arriving before Minecraft's systems had fully settled.",
                "A prototype world, a track, and a cart that says the future is coming.",
                wiki("Java_Edition_Infdev_20100618")));

        exact(n("c0.30_01c", "Final Classic in launcher form", "November 10, 2009", "Classic",
                "Classic 0.30 was the final Classic development release; the launcher's c0.30_01c is a later launcher-compatible recompile. It preserves the old Creative/Survival-test feel rather than modern survival Minecraft.",
                "Classic Creative and Survival-era behavior, depending on mode/build context.",
                "The launcher-visible build is a recompiled artifact, while gameplay is tied to the 2009 Classic 0.30 history.",
                "",
                "",
                "Do not expect modern survival systems. This is Classic-era Minecraft with very different assumptions.",
                "It matters because Classic is a different game shape: quick blocks, early menus, and almost no modern survival baggage.",
                "Old enough that the launcher should probably whisper when it opens it.",
                wiki("Java_Edition_Classic_0.30")));

        exact(n("c0.0.13a", "Early Classic public-era build", "May 22, 2009", "Classic",
                "Classic 0.0.13a sits near Minecraft's earliest public Classic period. The official launcher entry has archival caveats, and the available file may not match a clean original release exactly.",
                "Early Classic block-building behavior.",
                "Classic versions around this point were still changing terrain, controls, and basic presentation rapidly.",
                "",
                "",
                "The released 0.0.13a is not archived cleanly; launcher-visible files can have metadata or content caveats.",
                "It matters as a window into Minecraft before survival systems became the focus.",
                "A tiny early-web block toy becoming something bigger by the hour.",
                wiki("Java_Edition_Classic_0.0.13a")));

        exact(n("c0.0.13a_03", "Classic 0.0.13a development caveat", "May 22, 2009", "Classic",
                "Classic 0.0.13a_03 is one of the archived launcher-visible early Classic builds with important historical caveats. It is useful for preservation, but should not be treated as a fully polished release.",
                "Early Classic build behavior.",
                "The available launcher file reflects archival/recompile realities around early Classic.",
                "",
                "",
                "Very early Classic builds may differ from release-page descriptions and can have missing or altered original context.",
                "It helps show how quickly the earliest public Minecraft builds were changing.",
                "A fossil with visible seams, still worth looking at.",
                wiki("Java_Edition_Classic_0.0.13a_03")));

        exact(n("c0.0.11a", "Very early Classic", "May 17, 2009", "Classic",
                "Classic 0.0.11a belongs to the earliest public Classic line. Notes are intentionally sparse because the historical record for these tiny builds is narrow.",
                "Early Classic block placement and world interaction.",
                "Basic controls and presentation were still changing quickly.",
                "",
                "",
                "Sparse documentation and archival caveats are normal for this age of Minecraft.",
                "It matters because it shows Minecraft before survival, crafting, hunger, biomes, or most familiar systems existed.",
                "Almost pure blocks, before Minecraft knew how much it would become.",
                wiki("Java_Edition_Classic_0.0.11a")));

        exact(n("rd-161348", "Last archived pre-Classic", "May 16, 2009", "Pre-Classic",
                "rd-161348 is the last archived pre-Classic build. It added saplings and exists in the launcher with later archival quirks, including texture/signature differences.",
                "Saplings.",
                "The available launcher copy uses later archival packaging details rather than an untouched 2009 original.",
                "",
                "",
                "No unedited original is available through the modern launcher path. Expect prototype behavior.",
                "It is one of the last steps before Minecraft becomes recognizable as Classic.",
                "A sapling in a prototype world is a pretty good metaphor, honestly.",
                wiki("Java_Edition_pre-Classic_rd-161348")));

        exact(n("rd-20090515", "Launcher alias to rd-161348", "May 15, 2009", "Pre-Classic",
                "The official launcher-visible rd-20090515 entry is historically unusual because it downloads rd-161348 rather than a separate preserved build. Treat it as an archival alias, not a normal independent version.",
                "",
                "Launcher metadata points this entry at rd-161348-era files.",
                "",
                "",
                "This is not a clean independent historical build in the way later versions are.",
                "It matters because the manifest itself preserves some of Minecraft's messy archival history.",
                "Even the version list has archaeology layers.",
                wiki("Java_Edition_pre-Classic_rd-20090515")));

        exact(n("rd-132211", "Oldest launcher-visible build", "May 13, 2009", "Pre-Classic",
                "rd-132211 is the oldest version available through the modern Minecraft Launcher lineage. It already supports placing and destroying blocks and saving a level, but it is still a prototype with severe quirks.",
                "Block placing and destroying.|Level saving.",
                "Flat prototype level behavior with a very early build limit.",
                "",
                "",
                "Known quirks include camera/pivot oddities, lag near the ground, and the ability to place blocks inside the player.",
                "It matters because it is about as close as the official launcher-visible archive gets to Minecraft's starting line.",
                "Before survival, before crafting, before almost everything: just blocks and a beginning.",
                wiki("Java_Edition_pre-Classic_rd-132211")));

        family("b1.8", fallback("b1.8.x", "Adventure Update era", "September 2011", "Beta 1.8",
                "This fallback covers the Adventure Update Beta builds when an exact entry is missing. Hunger, sprinting, Creative mode, villages, strongholds, mineshafts, ravines, and Endermen define this era.",
                "Adventure Update systems such as hunger, sprinting, Creative mode, and new world structures.",
                "Survival pacing moved away from the older no-hunger Beta loop.",
                "Use exact b1.8 or b1.8.1 notes when available.",
                "",
                "Fallback summary only; exact historical notes for this build were not found.",
                "A major hinge between old Beta survival and modern-style Minecraft.",
                "Big new direction, clearly labeled when the exact patch is unknown.",
                wiki("Java_Edition_Beta_1.8")));

        family("b1.7", fallback("b1.7.x", "Piston-era Beta", "June-July 2011", "Beta 1.7",
                "This fallback covers the piston update family when exact notes are missing. Pistons, sticky pistons, and shears define the era, with b1.7.3 generally preferred for stable play.",
                "Pistons, sticky pistons, and shears.",
                "Late no-hunger Beta redstone play expanded sharply.",
                "Later b1.7.x builds fixed several piston-era bugs.",
                "",
                "Fallback summary only; exact historical notes for this build were not found.",
                "This is the last big pre-Adventure redstone era.",
                "Moving blocks, old survival, and a lot of nostalgia.",
                wiki("Java_Edition_Beta_1.7")));

        family("b1.6", fallback("b1.6.x", "Map Update era", "May 2011", "Beta 1.6",
                "This fallback covers the map update family. Maps, trapdoors, tall grass, and a rapid sequence of bug-fix builds define the line.",
                "Maps, trapdoors, tall grass, and dead bushes.",
                "Exploration gained map-making.",
                "Several fast follow-up patches repaired b1.6 launch issues.",
                "",
                "Fallback summary only; exact historical notes for this build were not found.",
                "Maps changed how players understood large worlds.",
                "The cartographer era of old Beta.",
                wiki("Java_Edition_Beta_1.6")));

        family("b1.5", fallback("b1.5.x", "Weather and achievements", "April 2011", "Beta 1.5",
                "This fallback covers the weather, statistics, achievements, and powered-rail update family.",
                "Weather, statistics, achievements, powered rails, and detector rails.",
                "World atmosphere and minecart transport changed substantially.",
                "b1.5_01 fixed several release issues.",
                "",
                "Fallback summary only; exact historical notes for this build were not found.",
                "Weather made Minecraft worlds feel less static.",
                "Rain on dirt, rails in tunnels, achievements in the corner.",
                wiki("Java_Edition_Beta_1.5")));

        family("b1.4", fallback("b1.4.x", "Wolf Update", "March-April 2011", "Beta 1.4",
                "This fallback covers the wolf update family, including wolves, cookies, and locked-chest cleanup.",
                "Wolves and cookies.",
                "Tamed companions became part of survival.",
                "b1.4_01 cleaned up locked-chest behavior and bed respawn issues.",
                "Locked chests were cleaned up after their joke appearance.",
                "Fallback summary only; exact historical notes for this build were not found.",
                "Wolves gave old Beta survival a companion system.",
                "Dog update. Enough said.",
                wiki("Java_Edition_Beta_1.4")));

        family("b1.3", fallback("b1.3.x", "Beds and McRegion", "February 2011", "Beta 1.3",
                "This fallback covers the bed, repeater, slab, and McRegion save-format update family.",
                "Beds, repeaters, slabs, and a new splash screen.",
                "World saves changed to McRegion.",
                "Follow-up builds fixed crashes and compatibility issues.",
                "",
                "Fallback summary only; exact historical notes for this build were not found.",
                "Beds changed survival pacing; McRegion changed world storage.",
                "A sleep update wrapped around a file-format update.",
                wiki("Java_Edition_Beta_1.3")));

        family("b1.2", fallback("b1.2.x", "Dyes and note blocks", "January 2011", "Beta 1.2",
                "This fallback covers the first big 2011 Beta content update: dyes, note blocks, dispensers, squid, lapis lazuli, sandstone, and related crafting materials.",
                "Dyes, note blocks, dispensers, squid, lapis lazuli, sandstone, sugar, bone meal, and charcoal.",
                "Decoration, color, sound, and redstone utility expanded together.",
                "",
                "",
                "Fallback summary only; exact historical notes for this build were not found.",
                "A large early-Beta feature burst.",
                "The world got color, sound, and squid.",
                wiki("Java_Edition_Beta_1.2")));

        family("b1.1", fallback("b1.1.x", "Early Beta maintenance", "December 2010", "Beta 1.1",
                "This fallback covers the narrow Beta 1.1 maintenance sequence just after Minecraft entered Beta.",
                "",
                "Early Beta behavior continued to stabilize.",
                "Small fixes around the first Beta builds.",
                "",
                "Fallback summary only; exact historical notes for this build were not found.",
                "Useful mainly for reproducing the first days of Beta.",
                "Tiny patches near a big label change.",
                wiki("Java_Edition_Beta_1.1")));

        family("b1.0", fallback("b1.0.x", "First Beta", "December 2010", "Beta 1.0",
                "This fallback covers Minecraft's move from Alpha into Beta.",
                "Working server-side inventory, cape/cloak support, and new splash text details.",
                "Minecraft entered the Beta development label.",
                "Early hotfixes repaired startup and platform issues.",
                "",
                "Fallback summary only; exact historical notes for this build were not found.",
                "The Beta label begins here.",
                "Alpha dirt wearing a Beta nametag.",
                wiki("Java_Edition_Beta_1.0")));

        family("a1.2", fallback("a1.2.x", "Halloween Update era", "October-December 2010", "Alpha 1.2",
                "This fallback covers the Alpha Halloween Update era: Nether, biomes, fishing, new mobs, and the final Alpha builds before Beta.",
                "Nether, biomes, fishing, pumpkins, and Nether-era blocks and mobs.",
                "World identity changed through biomes and Nether travel.",
                "Many follow-up builds repaired Halloween Update issues.",
                "",
                "Fallback summary only; exact historical notes for this build were not found.",
                "Alpha 1.2 made Minecraft feel much bigger and stranger.",
                "Portals and pumpkins, but with honest fallback labeling.",
                wiki("Java_Edition_Alpha_v1.2.0")));

        family("a1.1", fallback("a1.1.x", "Late Seecret Friday Alpha", "September 2010", "Alpha 1.1",
                "This fallback covers the last Seecret Friday period and the late pre-Halloween Alpha line.",
                "Compass and shared inventory behavior in the a1.1.0 line.",
                "Resource and sound handling changed in later a1.1.x builds.",
                "Sound and display bugs were repaired across the line.",
                "",
                "Fallback summary only; exact historical notes for this build were not found.",
                "Late Alpha was becoming more playable while still very experimental.",
                "The quiet before the Nether opened.",
                wiki("Java_Edition_Alpha_v1.1.0")));

        family("a1.0", fallback("a1.0.x", "Early Alpha", "July-August 2010", "Alpha 1.0",
                "This fallback covers early Alpha builds after Infdev, including Seecret Friday updates and early multiplayer/server behavior.",
                "Early Alpha blocks, mobs, resources, and multiplayer administration depending on exact build.",
                "The game moved from Infdev experiments into named Alpha development.",
                "Small rapid fixes are common in this period.",
                "",
                "Fallback summary only; exact historical notes for this build were not found.",
                "Early Alpha is where Minecraft begins looking like survival Minecraft, but not yet like Beta.",
                "Rough edges, bright grass, and very fast iteration.",
                wiki("Java_Edition_Alpha_v1.0.0")));

        family("inf-", fallback("infdev", "Infdev", "2010", "Infdev",
                "This fallback covers Infdev builds: experimental infinite-world Minecraft before Alpha.",
                "Experimental infinite-world systems, depending on exact build.",
                "Terrain and survival behavior were still in prototype form.",
                "",
                "",
                "Fallback summary only; exact historical notes for this build were not found.",
                "Infdev is where Minecraft's world scale begins to open up.",
                "Prototype Minecraft with a horizon that started running away.",
                wiki("Java_Edition_Infdev")));

        family("c0.", fallback("classic", "Classic", "2009", "Classic",
                "This fallback covers Classic builds: early creative/block-placement Minecraft before the later survival loop.",
                "Classic block-building behavior.",
                "Basic controls, block lists, and world behavior changed quickly.",
                "",
                "",
                "Fallback summary only; exact historical notes for this build were not found.",
                "Classic is a different shape of Minecraft, closer to a construction toy than later survival.",
                "Old-web block building, preserved carefully.",
                wiki("Java_Edition_Classic")));

        family("rd-", fallback("preclassic", "Pre-Classic", "May 2009", "Pre-Classic",
                "This fallback covers the earliest launcher-visible Minecraft prototypes.",
                "Prototype block placing, destroying, and level behavior depending on exact build.",
                "The game was changing hour by hour.",
                "",
                "",
                "Fallback summary only; exact historical notes for this build were not found.",
                "Pre-Classic shows Minecraft before most familiar systems existed.",
                "The fossil layer under Classic.",
                wiki("Java_Edition_pre-Classic")));
    }

    static Note forVersion(String rawVersion) {
        String version = safe(rawVersion);
        Note exact = EXACT.get(version);
        if (exact != null) {
            return exact;
        }
        String key = familyKey(version);
        Note fallback = FAMILY.get(key);
        if (fallback != null) {
            return fallbackFor(version, fallback);
        }
        return fallbackFor(version, null);
    }

    private static Note fallbackFor(String version, Note base) {
        if (base == null) {
            base = fallback("unknown", "Unknown legacy build", "", "Legacy",
                    "Exact historical notes for this build were not found. Showing a nearest verified era summary instead.",
                    "", "", "", "",
                    "The launcher could not match this version to a known static note.",
                    "This version may still launch if the required files are present locally.",
                    "A mystery build deserves a cautious note.",
                    MANIFEST);
        }
        return new Note(version, base.title, base.releaseDate, base.family, base.shortSummary,
                base.added, base.changed, base.fixed, base.removed, base.knownQuirks,
                base.whyItMatters, base.launcherCommentary, base.sources, false);
    }

    private static String familyKey(String version) {
        if (version == null) {
            return "unknown";
        }
        if (version.startsWith("b1.8")) return "b1.8";
        if (version.startsWith("b1.7")) return "b1.7";
        if (version.startsWith("b1.6")) return "b1.6";
        if (version.startsWith("b1.5")) return "b1.5";
        if (version.startsWith("b1.4")) return "b1.4";
        if (version.startsWith("b1.3")) return "b1.3";
        if (version.startsWith("b1.2")) return "b1.2";
        if (version.startsWith("b1.1")) return "b1.1";
        if (version.startsWith("b1.0")) return "b1.0";
        if (version.startsWith("a1.2")) return "a1.2";
        if (version.startsWith("a1.1")) return "a1.1";
        if (version.startsWith("a1.0")) return "a1.0";
        if (version.startsWith("inf-")) return "inf-";
        if (version.startsWith("c0.")) return "c0.";
        if (version.startsWith("rd-")) return "rd-";
        return "unknown";
    }

    private static void exact(Note note) {
        EXACT.put(note.versionId, note);
    }

    private static void family(String key, Note note) {
        FAMILY.put(key, note);
    }

    private static Note n(String versionId, String title, String releaseDate, String family,
                          String shortSummary, String added, String changed, String fixed,
                          String removed, String knownQuirks, String whyItMatters,
                          String launcherCommentary, String sources) {
        return new Note(versionId, title, releaseDate, family, shortSummary, added, changed,
                fixed, removed, knownQuirks, whyItMatters, launcherCommentary, sources, true);
    }

    private static Note fallback(String versionId, String title, String releaseDate, String family,
                                 String shortSummary, String added, String changed, String fixed,
                                 String removed, String knownQuirks, String whyItMatters,
                                 String launcherCommentary, String sources) {
        return new Note(versionId, title, releaseDate, family, shortSummary, added, changed,
                fixed, removed, knownQuirks, whyItMatters, launcherCommentary, sources, false);
    }

    private static String wiki(String page) {
        return "Minecraft Wiki=https://minecraft.wiki/w/" + page + "|" + MANIFEST;
    }

    private static String[] split(String text) {
        text = safe(text);
        if (text.length() == 0) {
            return new String[0];
        }
        return text.split("\\|");
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
