package dev.movi.pikastats.tab;

public final class MatchOverviewRegression {
    private MatchOverviewRegression() {}

    public static boolean run() {
        MatchOverview.update("§aBeds Destroyed: §f2 . §aKills: §f7 . §aFinal Kills: §f3");
        String text = MatchOverview.text();
        if (text == null || !text.contains("2") || !text.contains("7") || !text.contains("3"))
            return false;
        MatchOverview.update("Waiting for players");
        return MatchOverview.text() == null;
    }
}
