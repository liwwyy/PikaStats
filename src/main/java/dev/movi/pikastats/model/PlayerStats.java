package dev.movi.pikastats.model;

import java.util.Locale;
import net.minecraft.util.EnumChatFormatting;

public final class PlayerStats {
    public long timestamp = System.currentTimeMillis();
    public boolean failed, nicked, apiDisabled, noStats;
    public String failureReason = "", username = "";
    public Integer level, wins, losses, finalKills, finalDeaths, kills, deaths, beds, games, bestWinstreak;
    public EnumChatFormatting levelColor = EnumChatFormatting.GRAY;
    public String rank = "", rankShort = "";

    public PlayerStats() {}
    public PlayerStats(String username) {
        this.username = username;
    }

    public boolean hasProfile() {
        return level != null || (rank != null && !rank.isEmpty());
    }
    public boolean hasBedWarsStats() {
        return wins != null || losses != null || finalKills != null || finalDeaths != null || beds != null
            || bestWinstreak != null;
    }
    public double fkdr() {
        return finalKills == null ? Double.NaN
                                  : finalKills / (double) Math.max(1, finalDeaths == null ? 0 : finalDeaths);
    }
    public double wlr() {
        return wins == null ? Double.NaN : wins / (double) Math.max(1, losses == null ? 0 : losses);
    }

    public String levelText() {
        return level == null ? EnumChatFormatting.DARK_GRAY + "?" : levelColor.toString() + level;
    }
    public String rankText() {
        return rank == null || rank.trim().isEmpty() ? EnumChatFormatting.GRAY + "-" : rank;
    }
    public String rankShortText() {
        return rankShort == null || rankShort.trim().isEmpty() ? EnumChatFormatting.GRAY + "-" : rankShort;
    }
    public String fkdrText() {
        return ratio(fkdr(), new double[] {1, 2, 3, 5, 7.5, 10});
    }
    public String wlrText() {
        return ratio(wlr(), new double[] {0.5, 1, 2, 5, 7, 10});
    }
    public String winstreakText() {
        return threshold(bestWinstreak, new double[] {2, 5, 10, 25, 50, 100});
    }
    public String finalKillsText() {
        return number(finalKills);
    }
    public String winsText() {
        return number(wins);
    }
    public String bedsText() {
        return number(beds);
    }

    public String nametagValue(int mode) {
        switch (mode) {
            case 1:
                return levelText();
            case 2:
                return wlrText();
            case 3:
                return winstreakText();
            case 4:
                return threshold(finalKills, new double[] {100, 500, 1000, 5000, 10000, 25000});
            case 5:
                return threshold(wins, new double[] {10, 50, 100, 500, 1000, 5000});
            case 6:
                return threshold(beds, new double[] {50, 100, 500, 1000, 5000, 10000});
            default:
                return fkdrText();
        }
    }

    private String ratio(double value, double[] t) {
        if (Double.isNaN(value))
            return EnumChatFormatting.DARK_GRAY + "?";
        return colorFor(value, t) + String.format(Locale.US, "%.2f", value);
    }
    private String threshold(Integer value, double[] t) {
        if (value == null)
            return EnumChatFormatting.DARK_GRAY + "?";
        return colorFor(value.doubleValue(), t) + Integer.toString(value);
    }
    private String number(Integer value) {
        return value == null ? EnumChatFormatting.DARK_GRAY + "?"
                             : EnumChatFormatting.WHITE + Integer.toString(value);
    }
    private String colorFor(double value, double[] t) {
        EnumChatFormatting[] c = {EnumChatFormatting.GRAY, EnumChatFormatting.GREEN,
            EnumChatFormatting.YELLOW, EnumChatFormatting.GOLD, EnumChatFormatting.RED,
            EnumChatFormatting.DARK_RED, EnumChatFormatting.LIGHT_PURPLE};
        int idx = 0;
        for (int i = 0; i < t.length; i++)
            if (value >= t[i])
                idx = i + 1;
        return c[idx].toString();
    }

    public PlayerStats copy() {
        PlayerStats p = new PlayerStats(username);
        p.timestamp = timestamp;
        p.failed = failed;
        p.nicked = nicked;
        p.apiDisabled = apiDisabled;
        p.noStats = noStats;
        p.failureReason = failureReason;
        p.level = level;
        p.levelColor = levelColor;
        p.rank = rank;
        p.rankShort = rankShort;
        p.wins = wins;
        p.losses = losses;
        p.finalKills = finalKills;
        p.finalDeaths = finalDeaths;
        p.kills = kills;
        p.deaths = deaths;
        p.beds = beds;
        p.games = games;
        p.bestWinstreak = bestWinstreak;
        return p;
    }
}
