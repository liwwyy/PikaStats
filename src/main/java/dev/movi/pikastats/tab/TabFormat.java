package dev.movi.pikastats.tab;

import dev.movi.pikastats.config.PikaConfig;
import dev.movi.pikastats.denick.DenickRegistry;
import dev.movi.pikastats.model.PlayerStats;
import dev.movi.pikastats.party.PartyTracker;
import dev.movi.pikastats.util.PingDisplay;
import dev.movi.pikastats.util.PlayerListUtil;
import dev.movi.pikastats.util.ScoreboardUtil;
import java.util.*;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

/** Shared table column model for the TAB and HUD overlays. */
public final class TabFormat {
    public static final class Column {
        public final String id, header;
        public final boolean right;
        Column(String id, String header, boolean right) {
            this.id = id;
            this.header = header;
            this.right = right;
        }
    }
    private static final List<String> CANON =
        Arrays.asList("lv", "rank", "name", "fkdr", "wlr", "ws", "fk", "wins", "beds", "ping");
    private static final Map<String, String> ALIASES = new HashMap<String, String>();
    static {
        alias("lv", "lv", "level");
        alias("rank", "rank", "rankshort");
        alias("name", "name", "player", "username");
        alias("ws", "hws", "highestwinstreak", "ws", "winstreak", "bestwinstreak");
        alias("fkdr", "fkdr");
        alias("wlr", "wlr", "wl");
        alias("fk", "fk", "finalkills");
        alias("wins", "wins", "win");
        alias("beds", "beds", "bed", "bb");
        alias("ping", "ping", "latency");
    }
    private TabFormat() {}
    private static void alias(String id, String... xs) {
        for (String x : xs)
            ALIASES.put(x, id);
    }

    public static List<Column> tabColumns() { return parse(PikaConfig.tabColumnOrder, true); }
    public static List<Column> hudColumns() { return parse(PikaConfig.hudColumnOrder, false); }
    private static List<Column> parse(String order, boolean tab) {
        LinkedHashSet<String> seen = new LinkedHashSet<String>();
        if (order == null)
            order = "";
        for (String raw : order.split("[,|]")) {
            String id = normalize(raw);
            if (id != null && enabled(id, tab))
                seen.add(id);
        }
        for (String id : CANON)
            if (enabled(id, tab))
                seen.add(id);
        ArrayList<Column> out = new ArrayList<Column>();
        for (String id : seen)
            out.add(column(id));
        return out;
    }
    private static String normalize(String raw) {
        String compact =
            raw.trim().replaceFirst("_+$", "").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return compact.isEmpty() ? null : ALIASES.get(compact);
    }
    private static boolean enabled(String id, boolean tab) {
        if (tab) {
            if (id.equals("lv"))
                return PikaConfig.tabShowLevel;
            if (id.equals("rank"))
                return PikaConfig.tabShowRank && !PikaConfig.combineRankWithName;
            if (id.equals("name"))
                return PikaConfig.tabShowName;
            if (id.equals("ws"))
                return PikaConfig.tabShowWinstreak;
            if (id.equals("fkdr"))
                return PikaConfig.tabShowFkdr;
            if (id.equals("wlr"))
                return PikaConfig.tabShowWlr;
            if (id.equals("fk"))
                return PikaConfig.tabShowFinalKills;
            if (id.equals("wins"))
                return PikaConfig.tabShowWins;
            if (id.equals("beds"))
                return PikaConfig.tabShowBeds;
            if (id.equals("ping"))
                return PikaConfig.tabShowPing;
        } else {
            if (id.equals("lv"))
                return PikaConfig.hudShowLevel;
            if (id.equals("rank"))
                return PikaConfig.hudShowRank && !PikaConfig.combineRankWithName;
            if (id.equals("name"))
                return PikaConfig.hudShowName;
            if (id.equals("ws"))
                return PikaConfig.hudShowWinstreak;
            if (id.equals("fkdr"))
                return PikaConfig.hudShowFkdr;
            if (id.equals("wlr"))
                return PikaConfig.hudShowWlr;
            if (id.equals("fk"))
                return PikaConfig.hudShowFinalKills;
            if (id.equals("wins"))
                return PikaConfig.hudShowWins;
            if (id.equals("beds"))
                return PikaConfig.hudShowBeds;
            if (id.equals("ping"))
                return PikaConfig.hudShowPing;
        }
        return false;
    }
    private static Column column(String id) {
        if (id.equals("lv"))
            return new Column(id, "LV", true);
        if (id.equals("rank"))
            return new Column(id, "RANK", false);
        if (id.equals("name"))
            return new Column(id, "NAME", false);
        if (id.equals("ws"))
            return new Column(id, "HWS", true);
        if (id.equals("fkdr"))
            return new Column(id, "FKDR", true);
        if (id.equals("wlr"))
            return new Column(id, "WLR", true);
        if (id.equals("fk"))
            return new Column(id, "FK", true);
        if (id.equals("wins"))
            return new Column(id, "WINS", true);
        if (id.equals("beds"))
            return new Column(id, "BEDS", true);
        if (id.equals("ping"))
            return new Column(id, "PING", true);
        return new Column(id, id.toUpperCase(Locale.ROOT), false);
    }

    public static String header(Column c) { return EnumChatFormatting.YELLOW + c.header; }
    public static String status(PlayerStats s) {
        if (s != null && s.apiDisabled)
            return EnumChatFormatting.RED + "API DISABLED";
        if (s != null && s.nicked)
            return EnumChatFormatting.LIGHT_PURPLE + "NICKED";
        if (s != null && s.noStats)
            return EnumChatFormatting.GRAY + "NO STATS";
        return null;
    }
    public static String rank(PlayerStats s) {
        return s == null ? EnumChatFormatting.GRAY + "-" : s.rankText();
    }
    public static String combinedSecondary(NetworkPlayerInfo info, PlayerStats s) {
        String real = DenickRegistry.realName(PlayerListUtil.profileName(info));
        return real == null ? rank(s)
            : EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + real;
    }
    public static String name(NetworkPlayerInfo info) {
        String username = PlayerListUtil.profileName(info);
        if (info.getGameType() == net.minecraft.world.WorldSettings.GameType.SPECTATOR)
            return EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + username;
        if (PikaConfig.partyHighlightEnabled && PartyTracker.isInParty() &&
            PartyTracker.isMember(username) &&
            ScoreboardUtil.bedWarsState() == ScoreboardUtil.BedWarsState.WAITING)
            return EnumChatFormatting.LIGHT_PURPLE + username;

        // 1.8.9 servers frequently encode the actual player-name color in the
        // team prefix/display name instead of ScorePlayerTeam#getChatFormat().
        // Vanilla's own tab renderer respects that formatted string, so recover
        // the active color immediately before the username rather than falling
        // back to white for every player.
        try {
            IChatComponent display = info.getDisplayName();
            String formatted = display != null ? display.getFormattedText() : null;
            EnumChatFormatting color = activeColorAtUsername(formatted, username);
            if (color != null)
                return color + username;
        } catch (Throwable ignored) {
        }
        try {
            ScorePlayerTeam team = info.getPlayerTeam();
            if (team != null) {
                String formatted = ScorePlayerTeam.formatPlayerName(team, username);
                EnumChatFormatting color = activeColorAtUsername(formatted, username);
                if (color != null)
                    return color + username;
                EnumChatFormatting chat = team.getChatFormat();
                if (chat != null && chat.isColor())
                    return chat + username;
            }
        } catch (Throwable ignored) {
        }
        return EnumChatFormatting.WHITE + username;
    }

    private static EnumChatFormatting activeColorAtUsername(String formatted, String username) {
        if (formatted == null || formatted.isEmpty() || username == null || username.isEmpty())
            return null;
        int end = formatted.lastIndexOf(username);
        if (end < 0)
            end = formatted.length();
        EnumChatFormatting active = null;
        for (int i = 0; i + 1 < end; i++) {
            if (formatted.charAt(i) != '\u00a7')
                continue;
            char code = Character.toLowerCase(formatted.charAt(++i));
            if (code == 'r') {
                active = null;
                continue;
            }
            for (EnumChatFormatting f : EnumChatFormatting.values()) {
                String token = f.toString();
                if (f.isColor() && token.length() >= 2 &&
                    Character.toLowerCase(token.charAt(1)) == code) {
                    active = f;
                    break;
                }
            }
        }
        return active;
    }
    public static String cell(Column c, NetworkPlayerInfo info, PlayerStats s) {
        if (c.id.equals("name"))
            return name(info);
        if (c.id.equals("ping"))
            return PingDisplay.formatted(info);
        if (s == null)
            return EnumChatFormatting.DARK_GRAY + "?";
        if (c.id.equals("lv"))
            return s.levelText();
        if (c.id.equals("rank"))
            return rank(s);
        if (c.id.equals("ws"))
            return s.winstreakText();
        if (c.id.equals("fkdr"))
            return s.fkdrText();
        if (c.id.equals("wlr"))
            return s.wlrText();
        if (c.id.equals("fk"))
            return s.finalKillsText();
        if (c.id.equals("wins"))
            return s.winsText();
        if (c.id.equals("beds"))
            return s.bedsText();
        return EnumChatFormatting.DARK_GRAY + "?";
    }
    public static String demo(Column c, int row) {
        if (c.id.equals("lv"))
            return EnumChatFormatting.AQUA + "42";
        if (c.id.equals("rank"))
            return EnumChatFormatting.GOLD + "Titan";
        if (c.id.equals("name"))
            return EnumChatFormatting.WHITE + (row == 0 ? "liywy" : "movi6287");
        if (c.id.equals("ws"))
            return EnumChatFormatting.YELLOW + "7";
        if (c.id.equals("fkdr"))
            return EnumChatFormatting.YELLOW + "2.41";
        if (c.id.equals("wlr"))
            return EnumChatFormatting.GREEN + "1.20";
        if (c.id.equals("fk"))
            return "§f821";
        if (c.id.equals("wins"))
            return "§f312";
        if (c.id.equals("beds"))
            return "§f544";
        if (c.id.equals("ping"))
            return EnumChatFormatting.GREEN + "65ms";
        return "§7?";
    }
    public static int[] statusSpan(List<Column> cols) {
        int name = indexOf(cols, "name"), start = name >= 0 ? name + 1 : 0;
        int[] run = findRun(cols, start);
        return run != null ? run : findRun(cols, 0);
    }
    private static int[] findRun(List<Column> cols, int from) {
        int start = -1;
        for (int i = from; i < cols.size(); i++) {
            boolean ok = !cols.get(i).id.equals("name") && !cols.get(i).id.equals("ping");
            if (ok && start < 0)
                start = i;
            if (!ok && start >= 0)
                return new int[] {start, i - 1};
        }
        return start >= 0 ? new int[] {start, cols.size() - 1} : null;
    }
    private static int indexOf(List<Column> cols, String id) {
        for (int i = 0; i < cols.size(); i++)
            if (cols.get(i).id.equals(id))
                return i;
        return -1;
    }
}
