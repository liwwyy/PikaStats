package dev.movi.pikastats.hud;

/** Keeps a HUD drawable briefly after its visibility condition turns off. */
final class HudVisibilityMotion {
    private boolean showing;
    private long openedAt, closingAt;

    boolean update(boolean visible, long now, int hideDurationMs, boolean animateHide) {
        if (visible) {
            if (!showing) openedAt = now;
            showing = true;
            closingAt = 0L;
            return true;
        }
        if (showing) closingAt = now;
        showing = false;
        return animateHide && closingAt != 0L &&
            now - closingAt < Math.max(40, hideDurationMs) * 1_000_000L;
    }

    boolean hiding() { return !showing && closingAt != 0L; }

    float progress(long now, int showDurationMs, int hideDurationMs) {
        long start = hiding() ? closingAt : openedAt;
        int duration = Math.max(40, hiding() ? hideDurationMs : showDurationMs);
        float elapsed = Math.min(1f, Math.max(0f, (now - start) / (duration * 1_000_000f)));
        return hiding() ? 1f - elapsed : elapsed;
    }

    void reset() {
        showing = false;
        closingAt = 0L;
    }
}
