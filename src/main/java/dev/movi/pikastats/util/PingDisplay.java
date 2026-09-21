package dev.movi.pikastats.util;

import dev.movi.pikastats.config.PikaConfig;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.EnumChatFormatting;

/** Throttles visible ping changes without performing any network work. */
public final class PingDisplay {
    private static final int MAX = 256;
    private static final LinkedHashMap<String, Entry> CACHE =
        new LinkedHashMap<String, Entry>(64, .75f, true);
    private PingDisplay() {}

    public static synchronized String formatted(NetworkPlayerInfo info) {
        if (info == null)
            return EnumChatFormatting.DARK_GRAY + "-";
        String name = PlayerListUtil.profileName(info).toLowerCase(java.util.Locale.ROOT);
        long now = System.currentTimeMillis();
        Entry entry = CACHE.get(name);
        long interval = Math.max(250, Math.min(5000, PikaConfig.pingUpdateIntervalMs));
        if (entry == null || now - entry.at >= interval) {
            entry = new Entry(info.getResponseTime(), now);
            CACHE.put(name, entry);
            trim();
        }
        int ms = entry.ms;
        if (ms == 0)
            return EnumChatFormatting.DARK_GRAY + "?";
        if (ms < 0)
            return EnumChatFormatting.DARK_GRAY + "-";
        return PingColors.formatting(ms).toString() + ms + "ms";
    }

    public static synchronized void cleanUp() {
        long stale = System.currentTimeMillis() - 60_000L;
        Iterator<Map.Entry<String, Entry>> it = CACHE.entrySet().iterator();
        while (it.hasNext())
            if (it.next().getValue().at < stale)
                it.remove();
        trim();
    }

    private static void trim() {
        while (CACHE.size() > MAX) {
            Iterator<String> it = CACHE.keySet().iterator();
            if (!it.hasNext())
                break;
            it.next();
            it.remove();
        }
    }
    private static final class Entry {
        final int ms;
        final long at;
        Entry(int ms, long at) {
            this.ms = ms;
            this.at = at;
        }
    }
}
