package dev.movi.pikastats.api;

import com.google.gson.*;
import dev.movi.pikastats.config.PikaConfig;
import dev.movi.pikastats.denick.DenickRegistry;
import dev.movi.pikastats.model.PlayerStats;
import dev.movi.pikastats.util.LegacyText;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import net.minecraft.util.EnumChatFormatting;

/** Background-only Pika API client. Render paths only call peek(). */
public final class StatsManager {
    private static final String PROFILE_BASE = "https://stats.pika-network.net/api/profile/";
    private static final int MAX_CACHE_ENTRIES = 500;
    private static final int MAX_RESPONSE_BYTES = 1024 * 1024;
    private static final long MIN_RETRY_MS = 30_000L;
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,16}$");
    private static final ConcurrentHashMap<String, PlayerStats> CACHE =
        new ConcurrentHashMap<String, PlayerStats>();
    private static final Set<String> FETCHING =
        Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private static final ConcurrentHashMap<String, Long> LAST_ATTEMPT = new ConcurrentHashMap<String, Long>();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4, new ThreadFactory() {
        private int index;
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "PikaStats-Pika-API-" + (++index));
            t.setDaemon(true);
            return t;
        }
    });
    private static volatile long blockedUntil;
    private static volatile long lastCleanup;
    private static final List<String> RANK_PRIORITY = Arrays.asList("owner", "developer", "manager", "admin",
        "srmod", "moderator", "mod", "helper", "youtube", "twitch", "champion", "titan", "elite", "vip");

    private StatsManager() {}

    public static PlayerStats peek(String username) {
        username = DenickRegistry.resolve(username);
        if (!valid(username))
            return null;
        return CACHE.get(
            key(username, PikaConfig.bedWarsMode().apiName, PikaConfig.statsPeriodValue().apiName));
    }

    public static boolean isLoading(String username) {
        username = DenickRegistry.resolve(username);
        if (!valid(username)) return false;
        String k = key(username, PikaConfig.bedWarsMode().apiName,
                       PikaConfig.statsPeriodValue().apiName);
        return FETCHING.contains(k) || (CACHE.get(k) == null && LAST_ATTEMPT.containsKey(k));
    }

    public static void prime(Collection<String> usernames) {
        if (usernames == null)
            return;
        String mode = PikaConfig.bedWarsMode().apiName;
        String period = PikaConfig.statsPeriodValue().apiName;
        int limit = clamp(PikaConfig.maxPlayersToFetch, 1, 40), used = 0;
        HashSet<String> seen = new HashSet<String>();
        for (String username : usernames) {
            username = DenickRegistry.resolve(username);
            if (used >= limit || !valid(username))
                continue;
            String lower = username.toLowerCase(Locale.ROOT);
            if (!seen.add(lower))
                continue;
            used++;
            String k = key(username, mode, period);
            if (needsRefresh(k))
                schedule(username, mode, period, k);
        }
    }

    public static Future<PlayerStats> fetchNow(final String username) {
        if (!valid(username)) {
            return CompletableFuture.completedFuture(failure(username, "Invalid Minecraft username"));
        }
        final String mode = PikaConfig.bedWarsMode().apiName;
        final String period = PikaConfig.statsPeriodValue().apiName;
        final String k = key(username, mode, period);
        return EXECUTOR.submit(new Callable<PlayerStats>() {
            @Override
            public PlayerStats call() {
                PlayerStats previous = CACHE.get(k);
                PlayerStats fetched = fetch(username, mode, period);
                PlayerStats merged = merge(fetched, previous);
                CACHE.put(k, merged);
                return merged;
            }
        });
    }

    public static void clear() {
        CACHE.clear();
        LAST_ATTEMPT.clear();
    }
    public static void shutdown() {
        EXECUTOR.shutdownNow();
        try {
            EXECUTOR.awaitTermination(250L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public static void cleanUp() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < 30_000L)
            return;
        lastCleanup = now;
        long ttl = clamp(PikaConfig.cacheTtlSeconds, 120, 600) * 1000L;
        long stale = now - ttl * 3L;
        for (Map.Entry<String, PlayerStats> e : CACHE.entrySet())
            if (e.getValue().timestamp < stale) {
                CACHE.remove(e.getKey(), e.getValue());
                LAST_ATTEMPT.remove(e.getKey());
            }
        if (CACHE.size() > MAX_CACHE_ENTRIES) {
            int removeCount = CACHE.size() - MAX_CACHE_ENTRIES;
            ArrayList<Map.Entry<String, PlayerStats>> all =
                new ArrayList<Map.Entry<String, PlayerStats>>(CACHE.entrySet());
            Collections.sort(all, new Comparator<Map.Entry<String, PlayerStats>>() {
                @Override
                public int compare(Map.Entry<String, PlayerStats> a, Map.Entry<String, PlayerStats> b) {
                    return Long.compare(a.getValue().timestamp, b.getValue().timestamp);
                }
            });
            for (int i = 0; i < removeCount && i < all.size(); i++) {
                Map.Entry<String, PlayerStats> e = all.get(i);
                CACHE.remove(e.getKey(), e.getValue());
                LAST_ATTEMPT.remove(e.getKey());
            }
        }
    }

    private static boolean needsRefresh(String k) {
        long now = System.currentTimeMillis();
        if (blockedUntil > now || FETCHING.contains(k))
            return false;
        PlayerStats current = CACHE.get(k);
        long ttl = clamp(PikaConfig.cacheTtlSeconds, 120, 600) * 1000L;
        long effective = current != null && current.failed ? Math.min(ttl, MIN_RETRY_MS) : ttl;
        if (current != null && now - current.timestamp <= effective)
            return false;
        Long last = LAST_ATTEMPT.get(k);
        return last == null || now - last >= MIN_RETRY_MS;
    }

    private static void schedule(
        final String username, final String mode, final String period, final String k) {
        long now = System.currentTimeMillis();
        if (blockedUntil > now || !FETCHING.add(k))
            return;
        LAST_ATTEMPT.put(k, now);
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (blockedUntil <= System.currentTimeMillis())
                        CACHE.put(k, merge(fetch(username, mode, period), CACHE.get(k)));
                } catch (Throwable ignored) {
                } finally {
                    FETCHING.remove(k);
                }
            }
        });
    }

    private static PlayerStats merge(PlayerStats fetched, PlayerStats previous) {
        if (previous == null || fetched.nicked || fetched.apiDisabled)
            return fetched;
        if (fetched.failed)
            return previous;
        if (!fetched.hasProfile() && previous.hasProfile()) {
            fetched.level = previous.level;
            fetched.levelColor = previous.levelColor;
            fetched.rank = previous.rank;
            fetched.rankShort = previous.rankShort;
        }
        if (!fetched.noStats && !fetched.hasBedWarsStats() && previous.hasBedWarsStats()) {
            fetched.wins = previous.wins;
            fetched.losses = previous.losses;
            fetched.finalKills = previous.finalKills;
            fetched.finalDeaths = previous.finalDeaths;
            fetched.kills = previous.kills;
            fetched.deaths = previous.deaths;
            fetched.beds = previous.beds;
            fetched.games = previous.games;
            fetched.bestWinstreak = previous.bestWinstreak;
        }
        return fetched;
    }

    private static PlayerStats fetch(String username, String mode, String period) {
        PlayerStats out = new PlayerStats(username);
        boolean profileOk = false, statsOk = false;
        ArrayList<String> issues = new ArrayList<String>();
        try {
            String encoded = URLEncoder.encode(username, "UTF-8");
            String profileUrl = PROFILE_BASE + encoded;
            String statsUrl = profileUrl + "/leaderboard?type=bedwars&interval=" + period + "&mode=" + mode;
            try {
                parseProfile(requestJson(profileUrl), out, username);
                profileOk = true;
            } catch (ApiHttpException e) {
                if (e.status == 400 || e.status == 404)
                    return nicked(out, e.status);
                issues.add(shortMessage(e));
            } catch (Exception e) {
                issues.add(shortMessage(e));
            }
            try {
                parseLeaderboard(requestJson(statsUrl), out, username);
                statsOk = true;
                out.noStats = !out.hasBedWarsStats();
                if (out.noStats)
                    issues.add("No BedWars stats for this mode");
            } catch (ApiHttpException e) {
                if (e.status == 204)
                    return apiDisabled(out);
                if (e.status == 400 || e.status == 404) {
                    out.noStats = true;
                    statsOk = true;
                    issues.add("BedWars leaderboard HTTP " + e.status);
                } else
                    issues.add(shortMessage(e));
            } catch (Exception e) {
                issues.add(shortMessage(e));
            }
        } catch (Exception e) {
            issues.add(shortMessage(e));
        }
        out.timestamp = System.currentTimeMillis();
        out.failed = !profileOk && !statsOk;
        out.failureReason = joinDistinct(issues);
        return out;
    }

    private static JsonObject requestJson(String urlText) throws IOException {
        long now = System.currentTimeMillis();
        if (blockedUntil > now)
            throw new IOException(
                "Rate limited for " + Math.max(1L, (blockedUntil - now + 999L) / 1000L) + "s");
        IOException last = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) new URL(urlText).openConnection();
                con.setInstanceFollowRedirects(false);
                con.setConnectTimeout(7000);
                con.setReadTimeout(12000);
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("User-Agent", "PikaStats/1.0.1 Forge-1.8.9");
                int status = con.getResponseCode();
                if (status == 429) {
                    long wait = retryAfter(con.getHeaderField("Retry-After"));
                    blockedUntil = System.currentTimeMillis() + wait;
                    throw new IOException("Rate limited for " + Math.max(1L, wait / 1000L) + "s");
                }
                if (status == 204 || status == 400 || status == 404)
                    throw new ApiHttpException(status);
                if (status == 403)
                    throw new IOException("Pika API refused request (403)");
                if (status >= 500 && status <= 504 && attempt == 0) {
                    sleep(600);
                    continue;
                }
                if (status < 200 || status >= 300)
                    throw new IOException("Pika API HTTP " + status);
                InputStream in = con.getInputStream();
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n, total = 0;
                while ((n = in.read(buf)) >= 0) {
                    total += n;
                    if (total > MAX_RESPONSE_BYTES)
                        throw new IOException("Pika API response too large");
                    bout.write(buf, 0, n);
                }
                in.close();
                JsonElement parsed =
                    new JsonParser().parse(new String(bout.toByteArray(), StandardCharsets.UTF_8));
                if (!parsed.isJsonObject())
                    throw new IOException("Unexpected API response");
                return parsed.getAsJsonObject();
            } catch (ApiHttpException e) {
                throw e;
            } catch (IOException e) {
                last = e;
                if (attempt == 0 && transientError(e)) {
                    sleep(500);
                    continue;
                }
                throw e;
            } catch (RuntimeException e) {
                throw new IOException("Unreadable Pika API response", e);
            } finally {
                if (con != null)
                    con.disconnect();
            }
        }
        throw last == null ? new IOException("Pika API request failed") : last;
    }

    private static void parseProfile(JsonObject data, PlayerStats out, String fallback) throws IOException {
        if (!data.has("username"))
            throw new IOException("Unexpected profile response");
        out.username = string(data, "username", fallback);
        JsonObject rankObj = object(data, "rank");
        if (rankObj != null) {
            out.level = nullableInt(rankObj, "level");
            out.levelColor =
                LegacyText.firstColor(string(rankObj, "rankDisplay", "&7"), EnumChatFormatting.GRAY);
        }
        JsonArray ranks = array(data, "ranks");
        JsonObject best = chooseRank(ranks);
        if (best != null) {
            String raw = string(best, "displayName", string(best, "name", ""));
            out.rank = LegacyText.ampToSection(raw);
            String plain = LegacyText.plain(raw).replace("[", "").replace("]", "").trim();
            if (plain.length() > 6)
                plain = plain.substring(0, 6);
            out.rankShort = LegacyText.firstColor(raw, EnumChatFormatting.GRAY).toString() + plain;
        }
    }

    private static JsonObject chooseRank(JsonArray ranks) {
        if (ranks == null)
            return null;
        long now = System.currentTimeMillis();
        JsonObject best = null;
        int bestPriority = Integer.MAX_VALUE;
        for (JsonElement el : ranks)
            if (el.isJsonObject()) {
                JsonObject r = el.getAsJsonObject();
                String server = string(r, "server", "").toLowerCase(Locale.ROOT);
                if (!(server.equals("games") || server.equals("minigames") || server.equals("bedwars")
                        || server.equals("global") || server.equals("network") || server.equals("all")))
                    continue;
                Long expiry = nullableLong(r, "expiry");
                if (expiry != null && expiry > 0) {
                    long ms = expiry < 1_000_000_000_000L ? expiry * 1000L : expiry;
                    if (ms <= now)
                        continue;
                }
                int p = rankPriority(r);
                if (p < bestPriority) {
                    bestPriority = p;
                    best = r;
                }
            }
        return best;
    }

    private static int rankPriority(JsonObject r) {
        String label = LegacyText.plain(string(r, "name", "") + " " + string(r, "displayName", ""))
                           .toLowerCase(Locale.ROOT);
        for (int i = 0; i < RANK_PRIORITY.size(); i++)
            if (label.contains(RANK_PRIORITY.get(i)))
                return i;
        return 100;
    }

    private static void parseLeaderboard(JsonObject data, PlayerStats out, String username) {
        out.wins = stat(data, username, "Wins");
        out.losses = stat(data, username, "Losses");
        out.finalKills = stat(data, username, "Final kills");
        out.finalDeaths = stat(data, username, "Final deaths");
        out.kills = stat(data, username, "Kills");
        out.deaths = stat(data, username, "Deaths");
        out.beds = stat(data, username, "Beds destroyed", "Beds broken");
        out.games = stat(data, username, "Games played");
        out.bestWinstreak = stat(data, username, "Highest winstreak reached");
    }

    private static Integer stat(JsonObject data, String username, String... labels) {
        HashSet<String> wanted = new HashSet<String>();
        for (String l : labels) wanted.add(normalizeLabel(l));
        for (Map.Entry<String, JsonElement> e : data.entrySet()) {
            if (!wanted.contains(normalizeLabel(e.getKey())) || !e.getValue().isJsonObject())
                continue;
            JsonArray entries = array(e.getValue().getAsJsonObject(), "entries");
            if (entries == null)
                return null;
            for (JsonElement el : entries)
                if (el.isJsonObject()) {
                    JsonObject row = el.getAsJsonObject();
                    if (!string(row, "id", "").equalsIgnoreCase(username))
                        continue;
                    JsonElement v = row.get("value");
                    if (v == null || v.isJsonNull())
                        return null;
                    try {
                        double d = v.getAsDouble();
                        if (Double.isNaN(d) || Double.isInfinite(d) || d < 0)
                            return null;
                        return (int) Math.round(d);
                    } catch (Exception ex) {
                        return null;
                    }
                }
            return null;
        }
        return null;
    }

    private static PlayerStats nicked(PlayerStats p, int status) {
        DenickRegistry.reportUnresolved(p.username, "Pika API returned HTTP " + status);
        p.timestamp = System.currentTimeMillis();
        p.nicked = true;
        p.failed = false;
        p.failureReason = "Pika API HTTP " + status;
        return p;
    }
    private static PlayerStats apiDisabled(PlayerStats p) {
        p.timestamp = System.currentTimeMillis();
        p.apiDisabled = true;
        p.failed = false;
        p.failureReason = "Pika API disabled by player";
        return p;
    }
    private static PlayerStats failure(String u, String r) {
        PlayerStats p = new PlayerStats(u);
        p.failed = true;
        p.failureReason = r;
        return p;
    }
    private static boolean valid(String u) {
        return u != null && USERNAME.matcher(u).matches();
    }
    private static String key(String u, String m, String p) {
        return u.toLowerCase(Locale.ROOT) + "|" + m + "|" + p;
    }
    private static String normalizeLabel(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
    private static JsonObject object(JsonObject o, String k) {
        try {
            JsonElement e = o.get(k);
            return e != null && e.isJsonObject() ? e.getAsJsonObject() : null;
        } catch (Exception x) {
            return null;
        }
    }
    private static JsonArray array(JsonObject o, String k) {
        try {
            JsonElement e = o.get(k);
            return e != null && e.isJsonArray() ? e.getAsJsonArray() : null;
        } catch (Exception x) {
            return null;
        }
    }
    private static String string(JsonObject o, String k, String f) {
        try {
            JsonElement e = o.get(k);
            return e != null && !e.isJsonNull() ? e.getAsString() : f;
        } catch (Exception x) {
            return f;
        }
    }
    private static Integer nullableInt(JsonObject o, String k) {
        try {
            JsonElement e = o.get(k);
            return e != null && !e.isJsonNull() ? e.getAsInt() : null;
        } catch (Exception x) {
            return null;
        }
    }
    private static Long nullableLong(JsonObject o, String k) {
        try {
            JsonElement e = o.get(k);
            return e != null && !e.isJsonNull() ? e.getAsLong() : null;
        } catch (Exception x) {
            return null;
        }
    }
    private static long retryAfter(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (!value.isEmpty()) {
            try {
                long seconds = Long.parseLong(value);
                return Math.max(1_000L, Math.min(120_000L, seconds * 1000L));
            } catch (NumberFormatException ignored) {
                try {
                    long retryAt = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME)
                                       .toInstant()
                                       .toEpochMilli();
                    return Math.max(1_000L, Math.min(120_000L, retryAt - System.currentTimeMillis()));
                } catch (Exception ignoredDate) {
                }
            }
        }
        return 10_000L;
    }
    private static boolean transientError(IOException e) {
        String m = e.getMessage();
        if (m == null)
            return false;
        m = m.toLowerCase(Locale.ROOT);
        return m.contains("timed out") || m.contains("timeout") || m.contains("reset")
            || m.contains("refused") || m.contains("unreachable");
    }
    private static void sleep(long ms) throws IOException {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Pika API request interrupted", e);
        }
    }
    private static String shortMessage(Throwable t) {
        String m = t.getMessage();
        return m == null || m.trim().isEmpty() ? t.getClass().getSimpleName() : m;
    }
    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
    private static String joinDistinct(List<String> xs) {
        LinkedHashSet<String> s = new LinkedHashSet<String>(xs);
        StringBuilder b = new StringBuilder();
        for (String x : s) {
            if (b.length() > 0)
                b.append("; ");
            b.append(x);
        }
        return b.toString();
    }
    private static final class ApiHttpException extends IOException {
        final int status;
        ApiHttpException(int status) {
            super("Pika API HTTP " + status);
            this.status = status;
        }
    }
}
