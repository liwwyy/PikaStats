package dev.movi.pikastats.util;

import dev.movi.pikastats.api.StatsManager;
import dev.movi.pikastats.config.PikaConfig;
import dev.movi.pikastats.model.PlayerStats;
import dev.movi.pikastats.model.TabSortStat;
import dev.movi.pikastats.party.PartyTracker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.network.NetworkPlayerInfo;

public final class PlayerSorting {
    private PlayerSorting() {}
    public static List<NetworkPlayerInfo> sortPlayers(List<NetworkPlayerInfo> players) {
        return sortPlayers(players, true);
    }
    public static List<NetworkPlayerInfo> sortPlayers(List<NetworkPlayerInfo> players, boolean tab) {
        ScoreboardUtil.BedWarsState state = ScoreboardUtil.bedWarsState();
        if (state == ScoreboardUtil.BedWarsState.IN_GAME) return players;
        final boolean partyFirst = PikaConfig.partySortFirst && PartyTracker.isInParty();
        final boolean statSort = state == ScoreboardUtil.BedWarsState.WAITING &&
            (tab ? PikaConfig.tabSortEnabled : PikaConfig.hudSortEnabled);
        if (!partyFirst && !statSort)
            return players;
        final TabSortStat stat = TabSortStat.fromIndex(tab ? PikaConfig.tabSortStat : PikaConfig.hudSortStat);
        final ArrayList<Row> rows = new ArrayList<Row>();
        for (int i = 0; i < players.size(); i++) {
            NetworkPlayerInfo info = players.get(i);
            rows.add(new Row(info, i, partyFirst && PartyTracker.isMember(PlayerListUtil.profileName(info)),
                statSort ? value(info, stat) : null));
        }
        Collections.sort(rows, new Comparator<Row>() {
            @Override
            public int compare(Row a, Row b) {
                if (partyFirst && a.party != b.party)
                    return a.party ? -1 : 1;
                if (statSort && (a.score != null || b.score != null)) {
                    if (a.score == null)
                        return 1;
                    if (b.score == null)
                        return -1;
                    int c = Double.compare(b.score, a.score);
                    if (c != 0)
                        return c;
                }
                return Integer.compare(a.index, b.index);
            }
        });
        ArrayList<NetworkPlayerInfo> out = new ArrayList<NetworkPlayerInfo>();
        for (Row r : rows) out.add(r.info);
        return out;
    }
    private static Double value(NetworkPlayerInfo info, TabSortStat stat) {
        PlayerStats s = StatsManager.peek(PlayerListUtil.profileName(info));
        if (s == null || s.nicked || s.apiDisabled || s.failed)
            return null;
        switch (stat) {
            case FKDR:
                return Double.isNaN(s.fkdr()) ? null : s.fkdr();
            case WLR:
                return Double.isNaN(s.wlr()) ? null : s.wlr();
            case BEST_WINSTREAK:
                return s.bestWinstreak == null ? null : s.bestWinstreak.doubleValue();
            case FINAL_KILLS:
                return s.finalKills == null ? null : s.finalKills.doubleValue();
            case WINS:
                return s.wins == null ? null : s.wins.doubleValue();
            case BEDS:
                return s.beds == null ? null : s.beds.doubleValue();
            case LEVEL:
                return s.level == null ? null : s.level.doubleValue();
            default:
                return null;
        }
    }
    private static final class Row {
        final NetworkPlayerInfo info;
        final int index;
        final boolean party;
        final Double score;
        Row(NetworkPlayerInfo i, int x, boolean p, Double s) {
            info = i;
            index = x;
            party = p;
            score = s;
        }
    }
}
