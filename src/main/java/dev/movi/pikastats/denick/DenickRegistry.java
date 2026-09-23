package dev.movi.pikastats.denick;

import dev.movi.pikastats.config.PikaConfig;
import dev.movi.pikastats.util.ScoreboardUtil;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.network.play.server.S3EPacketTeams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Detects the ordered original-name to nick rewrite sent in BedWars waiting rooms. */
public final class DenickRegistry {
    private static final Logger LOG = LogManager.getLogger("PikaStats-Denick");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,16}$");
    private static final long ROSTER_WINDOW_NS = 30_000_000_000L;
    private static final long REPLACEMENT_WINDOW_NS = 1_500_000_000L;
    private static final Map<String, Map<String, Long>> ADDED = new HashMap<String, Map<String, Long>>();
    private static final Map<String, ArrayDeque<Pending>> REMOVED =
        new HashMap<String, ArrayDeque<Pending>>();
    private static final Map<String, String> REAL_BY_NICK = new HashMap<String, String>();
    private static final Map<String, String> LAST_STAGE = new HashMap<String, String>();
    private static int ignoredOutsideWaiting;
    private static int rosterEvents;
    private static String recentOutsideRemovalTeam, recentOutsideRemovalName;
    private static long recentOutsideRemovalAt;

    private static final class Pending {
        final String original;
        final long time;
        Pending(String original, long time) {
            this.original = original;
            this.time = time;
        }
    }

    private DenickRegistry() {}

    public static synchronized void observe(S3EPacketTeams packet) {
        if (!PikaConfig.denicking) return;
        observe(packet.getAction(), packet.getName(), packet.getPlayers(),
                ScoreboardUtil.isPikaNetwork() &&
                    ScoreboardUtil.bedWarsState() == ScoreboardUtil.BedWarsState.WAITING,
                System.nanoTime());
    }

    static synchronized void observe(int action, String team, Iterable<String> players,
                                     boolean waiting, long now) {
        if (!PikaConfig.denicking) return;
        if (action != 0 && action != 3 && action != 4) return;
        ArrayList<String> names = new ArrayList<String>();
        for (String player : players)
            if (valid(player)) names.add(player);
        if (names.isEmpty()) return;

        Map<String, Long> roster = ADDED.get(team);
        if (roster == null) {
            roster = new HashMap<String, Long>();
            ADDED.put(team, roster);
        }
        if (action == 0 || action == 3) {
            rosterEvents++;
            if (action == 3 && waiting && names.size() == 1)
                match(team, names.get(0), now);
            for (String name : names) {
                roster.put(lower(name), now);
                if (action == 3 && !waiting) {
                    String stage = "team add outside waiting room: team=" + team
                        + "; no confirmed original-name replacement";
                    if (recentOutsideRemovalTeam != null
                        && now - recentOutsideRemovalAt <= REPLACEMENT_WINDOW_NS)
                        stage += "; nearby removal=" + recentOutsideRemovalName + " from team "
                            + recentOutsideRemovalTeam
                            + (team.equals(recentOutsideRemovalTeam) ? " (same team, but outside waiting)"
                                : " (different team; cannot pair)");
                    LAST_STAGE.put(lower(name), stage);
                } else if (action == 0 || !LAST_STAGE.containsKey(lower(name)))
                    LAST_STAGE.put(lower(name), "seen in team " + team + " roster (action " + action + ", waiting=" + waiting + ")");
            }
            return;
        }

        if (!waiting) {
            ignoredOutsideWaiting++;
            for (String name : names) {
                LAST_STAGE.put(lower(name), "team " + team + " removal ignored because BedWars waiting state was not detected");
                recentOutsideRemovalTeam = team;
                recentOutsideRemovalName = name;
                recentOutsideRemovalAt = now;
            }
            debug("Ignored removal outside a BedWars waiting room: team={} players={}", team, names);
            return;
        }
        ArrayDeque<Pending> queue = REMOVED.get(team);
        if (queue == null) {
            queue = new ArrayDeque<Pending>();
            REMOVED.put(team, queue);
        }
        for (String name : names) {
            Long addedAt = roster.get(lower(name));
            if (addedAt != null && now - addedAt <= ROSTER_WINDOW_NS) {
                queue.addLast(new Pending(name, now));
                LAST_STAGE.put(lower(name), "removed from team " + team + "; waiting for replacement add");
            } else {
                LAST_STAGE.put(lower(name), "removed from team " + team + " without a recent roster entry");
                debug("Cannot denick after removal: {} was not observed in team {}'s recent roster",
                      name, team);
            }
        }
    }

    private static void match(String team, String nick, long now) {
        ArrayDeque<Pending> queue = REMOVED.get(team);
        if (queue == null) {
            LAST_STAGE.put(lower(nick), "replacement add in team " + team + " had no earlier removal");
            return;
        }
        while (!queue.isEmpty() && now - queue.peekFirst().time > REPLACEMENT_WINDOW_NS) {
            Pending expired = queue.removeFirst();
            LAST_STAGE.put(lower(expired.original), "replacement did not arrive in team " + team + " within 1500 ms");
            debug("Denick candidate expired: original={} team={} ageMs={}", expired.original, team,
                  (now - expired.time) / 1_000_000L);
        }
        if (queue.isEmpty()) {
            LAST_STAGE.put(lower(nick), "replacement add in team " + team + " had no removal within 1500 ms");
            return;
        }
        Pending candidate = queue.removeFirst();
        if (candidate.original.equalsIgnoreCase(nick)) return;
        REAL_BY_NICK.put(lower(nick), candidate.original);
        LAST_STAGE.put(lower(nick), "matched team " + team + " replacement to " + candidate.original);
        if (PikaConfig.debugLogging)
            LOG.info("Denicked {} -> {} from waiting-room team {}", nick, candidate.original, team);
    }

    public static synchronized void reportUnresolved(String nick, String reason) {
        if (PikaConfig.denicking && PikaConfig.debugLogging && realName(nick) == null) {
            LOG.warn("Could not denick {}: {}; stage={}", nick, reason, diagnosticStage(nick));
        }
    }

    static synchronized String diagnosticStage(String nick) {
        String stage = nick == null ? null : LAST_STAGE.get(lower(nick));
        return stage == null
            ? "no team roster event for this name (rosterEvents=" + rosterEvents
                + ", removalsIgnoredOutsideWaiting=" + ignoredOutsideWaiting + ")"
            : stage;
    }

    public static synchronized String realName(String displayedName) {
        if (!PikaConfig.denicking) return null;
        return displayedName == null ? null : REAL_BY_NICK.get(lower(displayedName));
    }

    public static synchronized String resolve(String displayedName) {
        String real = realName(displayedName);
        return real == null ? displayedName : real;
    }

    public static synchronized void clear() {
        ADDED.clear();
        REMOVED.clear();
        REAL_BY_NICK.clear();
        LAST_STAGE.clear();
        rosterEvents = 0;
        ignoredOutsideWaiting = 0;
        recentOutsideRemovalTeam = recentOutsideRemovalName = null;
        recentOutsideRemovalAt = 0L;
    }

    private static void debug(String message, Object... args) {
        if (PikaConfig.debugLogging) LOG.warn(message, args);
    }
    private static String lower(String value) { return value.toLowerCase(Locale.ROOT); }
    private static boolean valid(String value) {
        return value != null && USERNAME.matcher(value).matches();
    }
}
