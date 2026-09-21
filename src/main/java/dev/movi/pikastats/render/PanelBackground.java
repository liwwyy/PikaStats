package dev.movi.pikastats.render;

/**
 * One base panel, then the optional image, followed by caller-rendered text.
 */
public final class PanelBackground {
    private PanelBackground() {}
    public static void draw(float left, float top, float right, float bottom, int opacity,
                            boolean glass, BackgroundImage image) {
        if (!glass || !GlassBackground.draw(left, top, right, bottom, opacity))
            RenderUtil.roundedRect(left, top, right, bottom, 6,
                                   RenderUtil.withAlpha(0x101217, opacity));
        image.draw(left, top, right, bottom);
    }
}
