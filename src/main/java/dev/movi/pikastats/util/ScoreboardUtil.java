package dev.movi.pikastats.util;

import dev.movi.pikastats.config.PikaConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

/** Pika/BedWars detection using only client-visible 1.8.9 state. */
public final class ScoreboardUtil {
    public enum BedWarsState { NONE, WAITING, IN_GAME }
    private static final Pattern MAP_WORD = Pattern.compile("(^|[^a-z0-9])map([^a-z0-9]|$)");
    private ScoreboardUtil() {}

    public static boolean shouldRenderTab() {
        if (!PikaConfig.isModEnabled() || PikaConfig.INSTANCE.tabHud == null
            || !PikaConfig.INSTANCE.tabHud.isEnabled())
            return false;
        if (!allowedContext())
            return false;
        if (PikaConfig.tabAlwaysShow)
            return true;
        BedWarsState state = bedWarsState();
        return state == BedWarsState.WAITING
            ? PikaConfig.tabShowWaiting
            : state == BedWarsState.IN_GAME && PikaConfig.tabShowInGame;
    }

    public static boolean shouldRenderHud() {
        if (!PikaConfig.isModEnabled() ||
            (PikaConfig.INSTANCE == null || !PikaConfig.INSTANCE.statsHud.isEnabled()))
            return false;
        if (!allowedContext())
            return false;
        if (PikaConfig.hudAlwaysShow)
            return true;
        BedWarsState state = bedWarsState();
        return state == BedWarsState.WAITING
            ? PikaConfig.hudShowWaiting
            : state == BedWarsState.IN_GAME && PikaConfig.hudShowInGame;
    }

    public static boolean shouldFetchStats() {
        return shouldRenderTab() || shouldRenderHud() || shouldRenderNametags();
    }

    public static boolean shouldRenderNametags() {
        return PikaConfig.isModEnabled() && PikaConfig.nametagsEnabled &&
            allowedContext() &&
            (PikaConfig.nametagsAlwaysShow || nametagStateAllowed(bedWarsState(), PikaConfig.nametagsShowWaiting,
                                PikaConfig.nametagsShowInGame));
    }

    public static boolean nametagStateAllowed(BedWarsState state, boolean waiting, boolean inGame) {
        return state == BedWarsState.WAITING ? waiting : state == BedWarsState.IN_GAME && inGame;
    }

    public static boolean allowedContext() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.theWorld != null && mc.thePlayer != null
            && (!PikaConfig.onlyOnPika || isPikaNetwork())
            && (!PikaConfig.bedWarsOnly || hasBedWarsScoreboard());
    }

    public static boolean hasBedWarsScoreboard() {
        for (String line : visibleSidebarStrings())
            if (normalize(line).contains("bedwars")) return true;
        return false;
    }

    public static boolean isPikaNetwork() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.isSingleplayer())
            return false;
        ServerData data = mc.getCurrentServerData();
        if (data == null || data.serverIP == null)
            return false;
        String host = data.serverIP.toLowerCase(Locale.ROOT).trim();
        while (host.endsWith("."))
            host = host.substring(0, host.length() - 1);
        if (host.startsWith("[")) {
            int close = host.indexOf(']');
            if (close > 0)
                host = host.substring(1, close);
        } else {
            int colon = host.indexOf(':');
            if (colon >= 0)
                host = host.substring(0, colon);
        }
        return host.equals("pika-network.net") || host.endsWith(".pika-network.net") ||
            host.equals("pika.host") || host.endsWith(".pika.host");
    }

    public static BedWarsState bedWarsState() {
        List<String> lines = visibleSidebarStrings();
        if (lines.isEmpty())
            return BedWarsState.NONE;
        boolean map = false;
        for (String raw : lines) {
            String value = normalize(raw);
            if (value.contains("red"))
                return BedWarsState.IN_GAME;
            if (MAP_WORD.matcher(value).find())
                map = true;
        }
        return map ? BedWarsState.WAITING : BedWarsState.NONE;
    }

    public static List<String> visibleSidebarStrings() {
        ArrayList<String> out = new ArrayList<String>();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null)
            return out;
        Scoreboard board = mc.theWorld.getScoreboard();
        if (board == null)
            return out;
        ScoreObjective objective = null;
        try {
            if (mc.thePlayer != null) {
                ScorePlayerTeam localTeam = board.getPlayersTeam(mc.thePlayer.getName());
                if (localTeam != null) {
                    int colorIndex = localTeam.getChatFormat().getColorIndex();
                    if (colorIndex >= 0)
                        objective = board.getObjectiveInDisplaySlot(3 + colorIndex);
                }
            }
        } catch (Throwable ignored) {
        }
        if (objective == null)
            objective = board.getObjectiveInDisplaySlot(1);
        if (objective == null)
            return out;
        add(out, objective.getDisplayName());
        try {
            Collection<Score> scores = board.getSortedScores(objective);
            int skipped = Math.max(0, scores.size() - 15);
            int index = 0;
            for (Score score : scores) {
                if (index++ < skipped)
                    continue;
                String owner = score.getPlayerName();
                if (owner == null || owner.startsWith("#"))
                    continue;
                ScorePlayerTeam team = board.getPlayersTeam(owner);
                add(out, ScorePlayerTeam.formatPlayerName(team, owner));
            }
        } catch (Throwable ignored) {
        }
        return out;
    }

    private static void add(List<String> out, String raw) {
        String clean = LegacyText.plain(raw).replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
        if (!clean.isEmpty())
            out.add(clean);
    }
    private static String normalize(String raw) {
        return LegacyText.plain(raw)
            .replace('\u00A0', ' ')
            .toLowerCase(Locale.ROOT)
            .replaceAll("\\s+", " ")
            .trim();
    }
}
