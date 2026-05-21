package net.minecraft;

import java.util.Random;

final class SplashText {
    private static final String[] SPLASHES = new String[] {
            "Now with fewer password boxes!",
            "Removed Herobrine from OAuth!",
            "Try Scrol- I mean, Caller's Bane for free!",
            "Made of dirt and TLS!",
            "Java 7 bytecode-ish!",
            "Ask your parents before going online!",
            "LWJGL goblins pacified!",
            "Classic vibes, modern tokens!",
            "No hunger? No problem!",
            "Unofficial alpha build!",
            "Now in block-o-vision!",
            "Do not feed the creepers!",
            "OAuth backstage!",
            "Singleplayer still works!",
            "Beta nostalgia included!",
            "Punch trees responsibly!",
            "Now with 100% more old grass!",
            "Creepers hate this one trick!",
            "Your saves are probably fine!",
            "Tell your wolf to sit!",
            "Pre-hunger certified!",
            "Now serving b1.7.3 energy!",
            "Infinite worlds, finite patience!",
            "Do not stare at Endermen!",
            "Still knows where .minecraft lives!",
            "Still punching trees!",
            "Beta never left!",
            "Powered by nostalgia!",
            "Now with extra dirt!",
            "No password boxes here!",
            "Built for blockheads!",
            "Pre-hunger approved!",
            "Offline mode lives!",
            "Respect the golden age!",
            "OAuth in a trench coat!",
            "Java goblins contained!",
            "Works on my XP machine!",
            "Classic grass included!",
            "Bring your own diamonds!",
            "Still scared of caves!",
            "Redstone before tutorials!",
            "Sheep were harmed for wool!",
            "Powered by questionable memories!",
            "Old launcher, new tricks!",
            "Now loading 2011 feelings!",
            "Mind the creeper!",
            "Beds are luxury tech!",
            "Wolves still sit forever!",
            "Singleplayer supremacy!",
            "Beta 1.7.3 says hi!",
            "No realms, just vibes!",
            "Fresh from .minecraft!",
            "Do not delete system32!",
            "More dirt than Chrome!",
            "Still hates bad Java!",
            "Now with sensible token storage!",
            "Microsoft login, Minecraft soul!",
            "Your saves matter!",
            "Craft first, ask later!",
            "Warning: nostalgia detected!",
            "Far Lands not included!",
            "Alpha energy preserved!",
            "Infdev walked so Beta ran!",
            "Launcher revived, Herobrine denied!",
            "Runs better with snacks!",
            "Old LWJGL, brave heart!",
            "Click Play, feel young!",
            "Still unofficial!",
            "No telemetry, just blocks!",
            "Classic UI supremacy!",
            "Built from recovered vibes!",
            "Modern auth, ancient dirt!",
            "Patch notes got buffed!",
            "Do not anger the natives!",
            "Welcome back, miner!"
    };

    private static final Random RANDOM = new Random();

    private SplashText() {
    }

    static String random() {
        return SPLASHES[RANDOM.nextInt(SPLASHES.length)];
    }

    static String forKey(String key) {
        if (key == null) {
            return random();
        }
        int index = Math.abs(key.hashCode());
        return SPLASHES[index % SPLASHES.length];
    }
}
