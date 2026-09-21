package dev.movi.pikastats.hud;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Time-based panel sizing and per-player entrances, independent of GL state. */
final class HudMotion {
    private float fromW, fromH, targetW, targetH;
    private long changedAt;
    private boolean initialized;
    private final Map<String, Long> joinedAt = new HashMap<String, Long>();
    private final Map<String, Long> leftAt = new HashMap<String, Long>();

    void update(float width, float height, List<String> names, long now, int durationMs,
                int exitDurationMs) {
        if (!initialized) {
            fromW = targetW = width;
            fromH = targetH = height;
            for (String name : names) joinedAt.put(name, 0L);
            initialized = true;
        } else {
            if (width != targetW || height != targetH) {
                fromW = width(now, durationMs);
                fromH = height(now, durationMs);
                targetW = width;
                targetH = height;
                changedAt = now;
            }
            Set<String> present = new HashSet<String>(names);
            for (String name : joinedAt.keySet())
                if (!present.contains(name)) leftAt.put(name, now);
            joinedAt.keySet().retainAll(present);
            for (String name : present) {
                leftAt.remove(name);
                if (!joinedAt.containsKey(name)) joinedAt.put(name, now);
            }
            leftAt.entrySet().removeIf(e -> progress(now, e.getValue(), exitDurationMs) >= 1f);
        }
    }
    Set<String> leaving(long now, int durationMs) {
        Set<String> result = new HashSet<String>();
        for (Map.Entry<String, Long> entry : leftAt.entrySet())
            if (progress(now, entry.getValue(), durationMs) < 1f) result.add(entry.getKey());
        return result;
    }
    float exit(String name, long now, int durationMs) {
        Long start = leftAt.get(name);
        return start == null ? 1f : ease(progress(now, start, durationMs));
    }

    float width(long now, int durationMs) {
        return fromW + (targetW - fromW) * ease(progress(now, changedAt, durationMs));
    }

    float height(long now, int durationMs) {
        return fromH + (targetH - fromH) * ease(progress(now, changedAt, durationMs));
    }

    float entry(String name, long now, int durationMs) {
        Long start = joinedAt.get(name);
        return start == null || start == 0L ? 1f : ease(progress(now, start, durationMs));
    }

    boolean entering(long now, int durationMs) {
        for (Long start : joinedAt.values())
            if (start != 0L && progress(now, start, durationMs) < 1f) return true;
        return false;
    }

    private static float progress(long now, long start, int durationMs) {
        if (start == 0L || durationMs <= 0) return 1f;
        return Math.min(1f, Math.max(0f, (now - start) / (durationMs * 1_000_000f)));
    }

    private static float ease(float t) { return t * t * (3f - 2f * t); }
}
