package dev.movi.pikastats.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.world.WorldSettings;

public final class PlayerListUtil {
    private PlayerListUtil() {}
    public static List<NetworkPlayerInfo> listedPlayers() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getNetHandler() == null)
            return Collections.emptyList();
        ArrayList<NetworkPlayerInfo> out =
            new ArrayList<NetworkPlayerInfo>(mc.getNetHandler().getPlayerInfoMap());
        for (int i = out.size() - 1; i >= 0; i--)
            if (profileName(out.get(i)).isEmpty())
                out.remove(i);
        Collections.sort(out, new Comparator<NetworkPlayerInfo>() {
            @Override
            public int compare(NetworkPlayerInfo a, NetworkPlayerInfo b) {
                boolean as = a.getGameType() == WorldSettings.GameType.SPECTATOR;
                boolean bs = b.getGameType() == WorldSettings.GameType.SPECTATOR;
                if (as != bs)
                    return as ? 1 : -1;
                String at = a.getPlayerTeam() == null ? "" : a.getPlayerTeam().getRegisteredName();
                String bt = b.getPlayerTeam() == null ? "" : b.getPlayerTeam().getRegisteredName();
                int team = at.compareToIgnoreCase(bt);
                if (team != 0)
                    return team;
                return profileName(a).compareToIgnoreCase(profileName(b));
            }
        });
        return out;
    }
    public static String profileName(NetworkPlayerInfo info) {
        return info == null || info.getGameProfile() == null || info.getGameProfile().getName() == null
            ? ""
            : info.getGameProfile().getName();
    }
}
