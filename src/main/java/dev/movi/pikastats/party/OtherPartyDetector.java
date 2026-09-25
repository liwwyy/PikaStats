package dev.movi.pikastats.party;

import dev.movi.pikastats.config.PikaConfig;
import dev.movi.pikastats.util.ScoreboardUtil;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.play.server.S38PacketPlayerListItem;

/** A cautious join-time hint; simultaneous server batching can create false groups. */
public final class OtherPartyDetector {
    private static final Map<String, Long> JOINED = new HashMap<String, Long>();
    private static boolean ready;
    private OtherPartyDetector() {}

    public static synchronized void tick() {
        if (!PikaConfig.detectOtherParties) { clear(); return; }
        ScoreboardUtil.BedWarsState state = ScoreboardUtil.bedWarsState();
        if (state == ScoreboardUtil.BedWarsState.IN_GAME) { clear(); return; }
        if (state == ScoreboardUtil.BedWarsState.WAITING) ready = true;
    }

    public static synchronized void observe(S38PacketPlayerListItem packet) {
        if (!PikaConfig.detectOtherParties || !ready || !ScoreboardUtil.isPikaNetwork()) return;
        if (packet.getAction() == S38PacketPlayerListItem.Action.REMOVE_PLAYER) {
            for (S38PacketPlayerListItem.AddPlayerData entry : packet.getEntries())
                if (entry.getProfile() != null && entry.getProfile().getName() != null)
                    JOINED.remove(entry.getProfile().getName().toLowerCase(Locale.ROOT));
            return;
        }
        if (packet.getAction() != S38PacketPlayerListItem.Action.ADD_PLAYER) return;
        long now = System.currentTimeMillis();
        for (S38PacketPlayerListItem.AddPlayerData entry : packet.getEntries()) {
            if (entry.getProfile() != null && entry.getProfile().getName() != null)
                JOINED.put(entry.getProfile().getName().toLowerCase(Locale.ROOT), now);
        }
    }

    public static synchronized int colorIndex(String username) {
        if (!PikaConfig.detectOtherParties || username == null ||
            ScoreboardUtil.bedWarsState() != ScoreboardUtil.BedWarsState.WAITING) return -1;
        if (PartyTracker.isMember(username)) return -1;
        Map<String, Long> others = new HashMap<String, Long>(JOINED);
        for (String player : JOINED.keySet())
            if (PartyTracker.isMember(player)) others.remove(player);
        return groupIndex(others, username, PikaConfig.otherPartyWindowMs);
    }

    static int groupIndex(Map<String, Long> joined, String username, int windowMs) {
        if (username == null || !joined.containsKey(username.toLowerCase(Locale.ROOT))) return -1;
        ArrayList<Map.Entry<String, Long>> arrivals =
            new ArrayList<Map.Entry<String, Long>>(joined.entrySet());
        Collections.sort(arrivals, new Comparator<Map.Entry<String, Long>>() {
            @Override public int compare(Map.Entry<String, Long> a, Map.Entry<String, Long> b) {
                int time = Long.compare(a.getValue(), b.getValue());
                return time == 0 ? a.getKey().compareTo(b.getKey()) : time;
            }
        });
        int color = 0;
        for (int start = 0; start < arrivals.size();) {
            int end = start + 1;
            while (end < arrivals.size() &&
                arrivals.get(end).getValue() - arrivals.get(start).getValue() <= windowMs) end++;
            if (end - start >= 2) {
                for (int i = start; i < end; i++)
                    if (arrivals.get(i).getKey().equalsIgnoreCase(username)) return color % 8;
                color++;
            }
            start = end;
        }
        return -1;
    }

    public static synchronized void clear() { JOINED.clear(); ready = false; }
}
