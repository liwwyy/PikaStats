package dev.movi.pikastats.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import dev.movi.pikastats.config.PikaConfig;
import dev.movi.pikastats.util.ScoreboardUtil;
import net.minecraft.client.Minecraft;

/** OneConfig-owned TAB table. It uses the same drawing path as the stats HUD. */
public final class TabHud extends Hud {
    private final HudVisibilityMotion visibility = new HudVisibilityMotion();

    public TabHud() {
        super(true, 960, 10, 2, 1);
        // OneConfig stores an offset from this center anchor. Start centered.
        position.setPosition(960 - getWidth(1, true) / 2, 10, 1920, 1080);
        ignoreCaching = true;
    }

    public void setLegacyEnabled(boolean value) { enabled = value; }

    @Override
    protected void preRender(boolean example) {
        StatsHudRenderer.refresh(true);
    }

    @Override
    protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
        StatsHudRenderer.renderAt(x, y, scale, example, true, example ? 1f : progress(),
            visibility.hiding() ? PikaConfig.tabHideAnimation : PikaConfig.tabAnimation);
    }

    @Override
    protected float getWidth(float scale, boolean example) {
        return StatsHudRenderer.width(example, true) * scale;
    }

    @Override
    protected float getHeight(float scale, boolean example) {
        return StatsHudRenderer.height(example, true) * scale;
    }

    @Override
    protected boolean shouldShow() {
        if (!isEnabled()) {
            visibility.reset();
            return false;
        }
        boolean visible = super.shouldShow() && ScoreboardUtil.shouldRenderTab()
            && Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isKeyDown();
        return visibility.update(visible, System.nanoTime(), PikaConfig.tabHideDuration,
            !PikaConfig.lowPerformanceMode && PikaConfig.tabHideAnimation != 0);
    }

    private float progress() {
        if (PikaConfig.lowPerformanceMode ||
            (!visibility.hiding() && PikaConfig.tabAnimation == 0)) return 1f;
        return visibility.progress(System.nanoTime(), PikaConfig.tabAnimationDuration,
                                   PikaConfig.tabHideDuration);
    }
}
