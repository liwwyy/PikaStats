package dev.movi.pikastats.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import java.util.Locale;

public final class PlayerHealth {
    private PlayerHealth() {}
    public static String text(String username) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || username == null) return "§8?";
        Scoreboard board = mc.theWorld.getScoreboard();
        ScoreObjective objective = board.getObjectiveInDisplaySlot(2);
        if (objective != null && isHealthObjective(objective) && board.entityHasObjective(username, objective))
            return "§c" + board.getValueFromObjective(username, objective).getScorePoints();
        EntityPlayer player = mc.theWorld.getPlayerEntityByName(username);
        if (player != null) return "§c" + Math.round(player.getHealth());
        return "§8?";
    }
    private static boolean isHealthObjective(ScoreObjective objective) {
        String label = objective.getDisplayName().toLowerCase(Locale.ROOT);
        return objective.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS
            || label.contains("health") || label.contains("hp") || label.contains("heart");
    }
}
