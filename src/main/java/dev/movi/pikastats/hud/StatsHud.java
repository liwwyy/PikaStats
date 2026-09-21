package dev.movi.pikastats.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.movi.pikastats.config.PikaConfig;
import dev.movi.pikastats.util.ScoreboardUtil;
import net.minecraft.client.Minecraft;

/** OneConfig owns rendering, persisted anchors, scaling and editor examples. */
public final class StatsHud extends Hud {
    private final HudVisibilityMotion visibility = new HudVisibilityMotion();
    public StatsHud() {
        super(true, 20, 20, 1);
        ignoreCaching = true;
    }
    @Override
    protected void preRender(boolean example) {
        StatsHudRenderer.refresh();
    }
    @Override
    protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
        StatsHudRenderer.renderAt(x, y, scale, example, false, example ? 1f : progress(),
            visibility.hiding() ? PikaConfig.hudHideAnimation : PikaConfig.hudAnimation);
    }
    @Override
    protected float getWidth(float scale, boolean example) {
        return StatsHudRenderer.width(example) * scale;
    }
    @Override
    protected float getHeight(float scale, boolean example) {
        return StatsHudRenderer.height(example) * scale;
    }
    @Override
    protected boolean shouldShow() {
        if (!isEnabled()) {
            visibility.reset();
            return false;
        }
        boolean visible = super.shouldShow() && ScoreboardUtil.shouldRenderHud()
            && !(PikaConfig.hideHudWhileTab
                && Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isKeyDown());
        return visibility.update(visible, System.nanoTime(), PikaConfig.hudHideDuration,
            !PikaConfig.lowPerformanceMode && PikaConfig.hudHideAnimation != 0);
    }

    private float progress() {
        if (PikaConfig.lowPerformanceMode ||
            (!visibility.hiding() && PikaConfig.hudAnimation == 0)) return 1f;
        return visibility.progress(System.nanoTime(), PikaConfig.hudAnimationDuration,
                                   PikaConfig.hudHideDuration);
    }
}
