package dev.movi.pikastats;

import dev.movi.pikastats.model.PlayerStats;
import dev.movi.pikastats.render.RenderUtil;
import dev.movi.pikastats.tab.TabFormat;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.*;
import javax.imageio.ImageIO;

/**
 * Headless checks run by Gradle check; no Minecraft client or network
 * required.
 */
public final class RegressionChecks {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition)
            throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        PlayerStats empty = new PlayerStats("Player");
        empty.noStats = true;
        check("§7NO STATS".equals(TabFormat.status(empty)),
              "Empty successful responses need a grey status");
        check(TabFormat.status(null) == null, "Pending stats must not be reported as absent");
        PlayerStats disabled = empty.copy();
        disabled.apiDisabled = true;
        check("§cAPI DISABLED".equals(TabFormat.status(disabled)), "Privacy status takes " +
                                                                     "priority");
        PlayerStats nicked = empty.copy();
        nicked.nicked = true;
        check("§dNICKED".equals(TabFormat.status(nicked)), "Nicked status takes priority");
        check(empty.copy().noStats, "Copies preserve no-stats state");
        PlayerStats zero = new PlayerStats("Player");
        zero.wins = 0;
        zero.losses = 0;
        zero.finalKills = 0;
        zero.finalDeaths = 0;
        check(zero.hasBedWarsStats() && TabFormat.status(zero) == null,
              "Explicit zero stats are valid");
        check(zero.fkdr() == 0 && zero.wlr() == 0, "Zero denominators must not produce infinity");
        check("§70".equals(zero.nametagValue(4)),
              "Low count nametags use the lowest threshold color");
        zero.finalKills = 25000;
        check("§d25000".equals(zero.nametagValue(4)),
              "High count nametags use the highest threshold color");
        PlayerStats previous = new PlayerStats("Player");
        previous.wins = 42;
        previous.finalKills = 100;
        Class<?> manager = Class.forName("dev.movi.pikastats.api.StatsManager");
        Method merge = manager.getDeclaredMethod("merge", PlayerStats.class, PlayerStats.class);
        merge.setAccessible(true);
        PlayerStats merged = (PlayerStats)merge.invoke(null, empty, previous);
        check(merged.noStats && merged.wins == null,
              "Confirmed empty responses must not resurrect cached numbers");
        PlayerStats failure = new PlayerStats("Player");
        failure.failed = true;
        check(merge.invoke(null, failure, previous) == previous,
              "Temporary failures retain previous successful stats");
        check(RenderUtil.withAlpha(0x123456, -1) == 0x123456, "Negative alpha clamps to zero");
        check(RenderUtil.withAlpha(0x123456, 101) == 0xff123456, "Alpha clamps to opaque");
        Class<?> backgrounds = Class.forName("dev.movi.pikastats.render.BackgroundImage");
        Method decode = backgrounds.getDeclaredMethod("decode", byte[].class);
        decode.setAccessible(true);
        BufferedImage source = new BufferedImage(2, 3, BufferedImage.TYPE_INT_ARGB);
        source.setRGB(1, 1, 0x80123456);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(source, "png", png);
        BufferedImage result = (BufferedImage)decode.invoke(null, (Object)png.toByteArray());
        check(result.getWidth() == 2 && result.getHeight() == 3, "PNG dimensions preserved");
        check(result.getRGB(0, 0) == 0 && result.getRGB(1, 1) == 0x80123456,
              "PNG cutouts and partial transparency preserved");
        reject(decode, new byte[] {1, 2, 3}, "Invalid image rejected");
        source = new BufferedImage(4097, 1, BufferedImage.TYPE_INT_ARGB);
        png.reset();
        ImageIO.write(source, "png", png);
        reject(decode, png.toByteArray(),
               "Oversized images rejected before allocating decoded pixels");
        for (dev.movi.pikastats.util.ScoreboardUtil.BedWarsState state :
             dev.movi.pikastats.util.ScoreboardUtil.BedWarsState.values()) {
            for (int switches = 0; switches < 4; switches++) {
                boolean waiting = (switches & 1) != 0, inGame = (switches & 2) != 0;
                boolean expected =
                    state == dev.movi.pikastats.util.ScoreboardUtil.BedWarsState.WAITING
                        ? waiting
                        : state == dev.movi.pikastats.util.ScoreboardUtil.BedWarsState.IN_GAME &&
                              inGame;
                check(dev.movi.pikastats.util.ScoreboardUtil.nametagStateAllowed(
                          state, waiting, inGame) == expected,
                      "Nametag visibility: " + state + " / " + switches);
            }
        }
        Object tab = backgrounds.getField("TAB").get(null),
               hud = backgrounds.getField("HUD").get(null);
        Field generation = backgrounds.getDeclaredField("generation");
        generation.setAccessible(true);
        Method invalidate = backgrounds.getDeclaredMethod("invalidate");
        invalidate.setAccessible(true);
        invalidate.invoke(tab);
        check(generation.getLong(tab) == 1 && generation.getLong(hud) == 0,
              "Reloading TAB must not invalidate HUD image requests");
        backgrounds.getMethod("reload").invoke(null);
        check(generation.getLong(tab) == 2 && generation.getLong(hud) == 1,
              "Reload assets invalidates both independent image slots");
        // Bypass the client-only constructor to exercise real migration code
        // headlessly.
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field singleton = unsafeClass.getDeclaredField("theUnsafe");
        singleton.setAccessible(true);
        dev.movi.pikastats.config.PikaConfig config =
            (dev.movi.pikastats.config.PikaConfig)unsafeClass
                .getMethod("allocateInstance", Class.class)
                .invoke(singleton.get(null), dev.movi.pikastats.config.PikaConfig.class);
        Method migrate = config.getClass().getDeclaredMethod("migrateSettings");
        migrate.setAccessible(true);
        dev.movi.pikastats.config.PikaConfig.overlayEnabled = true;
        dev.movi.pikastats.config.PikaConfig.imageFile = "legacy.png";
        config.enabled = false;
        migrate.invoke(config);
        check(config.enabled, "Legacy active TAB / disabled OneConfig must "
                                  + "restore the HUD master switch");
        check("legacy.png".equals(dev.movi.pikastats.config.PikaConfig.tabImageFile) &&
                  "legacy.png".equals(dev.movi.pikastats.config.PikaConfig.hudImageFile),
              "Legacy shared images migrate to both panels");
        config.enabled = false;
        dev.movi.pikastats.config.PikaConfig.tabImageFile = "tab.png";
        migrate.invoke(config);
        check(!config.enabled &&
                  "tab.png".equals(dev.movi.pikastats.config.PikaConfig.tabImageFile),
              "Migration never overwrites later master-switch or independent "
                  + "image choices");
        config.settingsVersion = 0;
        dev.movi.pikastats.config.PikaConfig.overlayEnabled = false;
        migrate.invoke(config);
        check(!config.enabled, "An intentionally disabled legacy overlay stays disabled");
        check(!dev.movi.pikastats.config.PikaConfig.nametagsEnabled &&
                  dev.movi.pikastats.config.PikaConfig.nametagsShowWaiting &&
                  !dev.movi.pikastats.config.PikaConfig.nametagsShowInGame,
              "Nametag master defaults off, with waiting visibility preselected");
        config.settingsVersion = 1;
        dev.movi.pikastats.config.PikaConfig.tabImagePosition = 1;
        dev.movi.pikastats.config.PikaConfig.hudImagePosition = 1;
        dev.movi.pikastats.config.PikaConfig.nametagsEnabled = true;
        dev.movi.pikastats.config.PikaConfig.nametagsShowWaiting = false;
        dev.movi.pikastats.config.PikaConfig.nametagsShowInGame = false;
        migrate.invoke(config);
        check(config.settingsVersion == 5 &&
                  dev.movi.pikastats.config.PikaConfig.tabImagePosition == 2 &&
                  dev.movi.pikastats.config.PikaConfig.hudImagePosition == 2,
              "Version 1 right-aligned images stay right-aligned after adding center");
        check(dev.movi.pikastats.config.PikaConfig.nametagsShowWaiting,
              "An enabled legacy nametag gets a visible waiting-lobby choice");
        dev.movi.pikastats.config.PikaConfig.tabShowLevel = false;
        dev.movi.pikastats.config.PikaConfig.hudShowLevel = true;
        check(TabFormat.hudColumns().size() == TabFormat.tabColumns().size() + 1,
              "TAB and HUD keep independent column visibility");
        config.settingsVersion = 2;
        dev.movi.pikastats.config.PikaConfig.fontMode = 1;
        dev.movi.pikastats.config.PikaConfig.customFont = "custom.tff";
        migrate.invoke(config);
        check(config.settingsVersion == 5 && dev.movi.pikastats.config.PikaConfig.fontMode == 2
                  && "custom.ttf".equals(dev.movi.pikastats.config.PikaConfig.customFont),
              "Old custom font selection and common extension typo migrate safely");
        config.settingsVersion = 3;
        dev.movi.pikastats.config.PikaConfig.fontMode = 1;
        migrate.invoke(config);
        check(config.settingsVersion == 5 && dev.movi.pikastats.config.PikaConfig.fontMode == 0,
              "Previous default Poppins profile moves to Minecraft font");
        config.settingsVersion = 4;
        dev.movi.pikastats.config.PikaConfig.tabAnimationDuration = 180;
        dev.movi.pikastats.config.PikaConfig.hudAnimationDuration = 180;
        migrate.invoke(config);
        check(config.settingsVersion == 5 &&
                  dev.movi.pikastats.config.PikaConfig.tabAnimationDuration == 120 &&
                  dev.movi.pikastats.config.PikaConfig.hudAnimationDuration == 120,
              "Existing default popup animations become faster for both tables");
        dev.movi.pikastats.tab.TabFormat.Column nameColumn =
            TabFormat.tabColumns().stream().filter(c -> c.id.equals("name")).findFirst().get();
        check(TabFormat.demo(nameColumn, 0).contains("liywy") &&
                  TabFormat.demo(nameColumn, 1).contains("movi6287"),
              "Editor previews show both requested usernames");
        check(dev.movi.pikastats.hud.HudMotionRegression.run(),
              "Panel resize and new-player animations interpolate and retarget smoothly");
        check(dev.movi.pikastats.hud.HudVisibilityRegression.run(),
              "HUD and TAB remain drawable through their hide animation");
        check(dev.movi.pikastats.render.CatboxCacheRegression.run(),
              "Shutdown clears only downloaded Catbox images and its selection marker");
        check(dev.movi.pikastats.denick.DenickRegression.run(),
              "Waiting-room team rewrites denick players without pairing unrelated packets");
        check(dev.movi.pikastats.tab.MatchOverviewRegression.run(),
              "Pika player-list match counters parse into the overview pill");
        backgrounds.getMethod("shutdown").invoke(null);
        manager.getMethod("shutdown").invoke(null);
        System.out.println("Passed " + checks + " regression checks.");
    }
    private static void reject(Method decode, byte[] data, String message) throws Exception {
        try {
            decode.invoke(null, (Object)data);
            throw new AssertionError(message);
        } catch (InvocationTargetException e) {
            check(e.getCause() instanceof IOException, message);
        }
    }
}
