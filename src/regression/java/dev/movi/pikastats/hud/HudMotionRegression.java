package dev.movi.pikastats.hud;

import java.util.Arrays;

public final class HudMotionRegression {
    private HudMotionRegression() {}

    public static boolean run() {
        HudMotion motion = new HudMotion();
        long start = 1_000_000_000L;
        motion.update(100, 24, Arrays.asList("liywy"), start, 100, 100);
        if (motion.entry("liywy", start, 100) != 1f) return false;
        motion.update(120, 36, Arrays.asList("liywy", "movi6287"), start + 1_000_000L, 100, 100);
        float halfway = motion.height(start + 51_000_000L, 100);
        if (halfway <= 24 || halfway >= 36) return false;
        if (motion.entry("movi6287", start + 1_000_000L, 100) != 0f) return false;
        if (!motion.entering(start + 51_000_000L, 100)) return false;
        if (motion.entry("movi6287", start + 101_000_000L, 100) != 1f) return false;
        if (motion.entering(start + 101_000_000L, 100)) return false;
        motion.update(100, 24, Arrays.asList("liywy"), start + 51_000_000L, 100, 100);
        if (!motion.leaving(start + 51_000_000L, 100).contains("movi6287")) return false;
        if (motion.exit("movi6287", start + 51_000_000L, 100) != 0f) return false;
        if (!motion.leaving(start + 151_000_000L, 100).isEmpty()) return false;
        return Math.abs(motion.height(start + 51_000_000L, 100) - halfway) < .01f &&
            Math.abs(motion.height(start + 151_000_000L, 100) - 24) < .01f;
    }
}
