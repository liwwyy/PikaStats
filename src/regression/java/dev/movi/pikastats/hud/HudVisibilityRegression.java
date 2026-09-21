package dev.movi.pikastats.hud;

public final class HudVisibilityRegression {
    private HudVisibilityRegression() {}

    public static boolean run() {
        HudVisibilityMotion motion = new HudVisibilityMotion();
        long start = 1_000_000_000L;
        if (!motion.update(true, start, 120, true)) return false;
        if (motion.progress(start, 120, 120) != 0f) return false;
        if (!motion.update(false, start + 120_000_000L, 120, true)) return false;
        if (!motion.hiding()) return false;
        float half = motion.progress(start + 180_000_000L, 120, 120);
        if (half < .49f || half > .51f) return false;
        if (motion.update(false, start + 240_000_000L, 120, true)) return false;
        if (!motion.update(true, start + 250_000_000L, 120, true)) return false;
        if (motion.hiding()) return false;
        motion.reset();
        return !motion.update(false, start + 260_000_000L, 120, true);
    }
}
