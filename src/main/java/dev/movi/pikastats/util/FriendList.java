package dev.movi.pikastats.util;

import dev.movi.pikastats.api.StatsManager;
import dev.movi.pikastats.model.PlayerStats;
import net.minecraft.client.Minecraft;
import java.util.Locale;

public final class FriendList {
    private FriendList() {}
    public static boolean contains(String username) {
        Minecraft mc = Minecraft.getMinecraft();
        if (username == null || mc == null || mc.thePlayer == null) return false;
        PlayerStats local = StatsManager.peek(mc.thePlayer.getName());
        return local != null && local.friends.contains(username.toLowerCase(Locale.ROOT));
    }
}
