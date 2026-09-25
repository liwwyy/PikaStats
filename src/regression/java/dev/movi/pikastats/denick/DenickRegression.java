package dev.movi.pikastats.denick;

import java.util.Arrays;

public final class DenickRegression {
    private DenickRegression() {}

    public static boolean run() {
        dev.movi.pikastats.config.PikaConfig.denicking = true;
        DenickRegistry.clear();
        long start = 1_000_000_000L;
        DenickRegistry.observe(3, "waiting", Arrays.asList("HandsomeproBoy", "RegularPlayer"),
                               true, start);
        DenickRegistry.observe(4, "waiting", Arrays.asList("HandsomeproBoy"), true,
                               start + 10_000_000L);
        DenickRegistry.observe(3, "waiting", Arrays.asList("Nimmuu"), true,
                               start + 15_000_000L);
        if (!"HandsomeproBoy".equals(DenickRegistry.realName("nimmuu"))) return false;
        DenickRegistry.observe(0, "second", Arrays.asList("EliteArmyGaming"), false,
                               start + 16_000_000L);
        DenickRegistry.observe(4, "second", Arrays.asList("EliteArmyGaming"), true,
                               start + 17_000_000L);
        DenickRegistry.observe(3, "second", Arrays.asList("Nammmu"), true,
                               start + 18_000_000L);
        if (!"EliteArmyGaming".equals(DenickRegistry.realName("Nammmu"))) return false;
        DenickRegistry.observe(4, "waiting", Arrays.asList("RegularPlayer"), true,
                               start + 20_000_000L);
        DenickRegistry.observe(3, "other-team", Arrays.asList("FakeNick"), true,
                               start + 21_000_000L);
        if (DenickRegistry.realName("FakeNick") != null) return false;
        DenickRegistry.observe(4, "waiting", Arrays.asList("RegularPlayer"), false,
                               start + 30_000_000L);
        DenickRegistry.observe(3, "waiting", Arrays.asList("AnotherNick"), false,
                               start + 31_000_000L);
        if (DenickRegistry.realName("AnotherNick") != null) return false;
        DenickRegistry.observe(3, "different-team", Arrays.asList("notPink_York_"), false,
                               start + 32_000_000L);
        if (DenickRegistry.realName("notPink_York_") != null
            || !DenickRegistry.diagnosticStage("notPink_York_").contains("different team; cannot pair"))
            return false;
        DenickRegistry.observe(3, "crowded", Arrays.asList("OriginalOne", "OriginalTwo"), true,
                               start + 40_000_000L);
        DenickRegistry.observe(4, "crowded", Arrays.asList("OriginalOne", "OriginalTwo"), true,
                               start + 41_000_000L);
        DenickRegistry.observe(3, "crowded", Arrays.asList("NickOne"), true,
                               start + 42_000_000L);
        return DenickRegistry.realName("NickOne") == null;
    }
}
