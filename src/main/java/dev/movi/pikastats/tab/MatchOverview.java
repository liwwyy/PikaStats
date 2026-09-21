package dev.movi.pikastats.tab;

import dev.movi.pikastats.util.LegacyText;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter;
import net.minecraft.util.IChatComponent;

/** Extracts Pika's match counters from the vanilla player-list header/footer. */
public final class MatchOverview {
    private static final Pattern BEDS = Pattern.compile("(?i)\\bbeds?\\s+destroyed\\s*[:：]?\\s*(\\d+)");
    private static final Pattern FINAL_KILLS = Pattern.compile("(?i)\\bfinal\\s+kills?\\s*[:：]?\\s*(\\d+)");
    private static final Pattern KILLS = Pattern.compile("(?i)(?<!final )\\bkills?\\s*[:：]?\\s*(\\d+)");
    private static volatile String text;

    private MatchOverview() {}

    public static void update(S47PacketPlayerListHeaderFooter packet) {
        IChatComponent header = packet.getHeader(), footer = packet.getFooter();
        update((header == null ? "" : header.getFormattedText()) + "\n" +
               (footer == null ? "" : footer.getFormattedText()));
    }

    static void update(String raw) {
        String plain = LegacyText.plain(raw).replace('\u00a0', ' ');
        Integer beds = value(BEDS, plain), kills = value(KILLS, plain), finals = value(FINAL_KILLS, plain);
        text = beds == null || kills == null || finals == null ? null
            : "§7Beds Destroyed: §f" + beds + " §8• §7Kills: §f" + kills +
              " §8• §7Final Kills: §f" + finals;
    }

    public static String text() { return text; }
    public static void clear() { text = null; }

    private static Integer value(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) return null;
        try { return Integer.valueOf(matcher.group(1)); }
        catch (NumberFormatException ignored) { return null; }
    }
}
