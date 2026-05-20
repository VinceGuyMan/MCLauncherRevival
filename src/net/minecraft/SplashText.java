package net.minecraft;

import java.util.Random;

final class SplashText {
    private static final String[] SPLASHES = new String[] {
            "Now with fewer password boxes!",
            "Removed Herobrine from OAuth!",
            "Try Scrol- I mean, Caller's Bane for free!",
            "Made of dirt and TLS!",
            "Java 8 compatible-ish!",
            "Ask your parents before going online!",
            "LWJGL goblins pacified!",
            "Classic vibes, modern tokens!",
            "No hunger? No problem!",
            "Codex #1 Vibe-Coder!",
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
            "Still knows where .minecraft lives!"
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
