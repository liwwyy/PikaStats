package dev.movi.pikastats.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * Reliable GUI primitives for legacy Forge.
 *
 * <p>Do not use GL_POLYGON here. Its smoothing/alpha behaviour varies wildly
 * across old drivers and client wrappers. Tessellator + POSITION_COLOR keeps
 * the HUD/TAB background predictable on 1.8.9 while still giving us smooth
 * rounded corners.</p>
 */
public final class RenderUtil {
    private static final int CORNER_SEGMENTS = 12;

    private RenderUtil() {}

    public static int withAlpha(int rgb, int opacityPercent) {
        int alpha = Math.max(0, Math.min(100, opacityPercent)) * 255 / 100;
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    public static void roundedRect(
        double left, double top, double right, double bottom, double radius, int argb) {
        if (right <= left || bottom <= top || ((argb >>> 24) & 255) == 0)
            return;
        double r = Math.max(0.0D, Math.min(radius, Math.min((right - left) / 2.0D, (bottom - top) / 2.0D)));
        if (r <= 0.0D) {
            rect(left, top, right, bottom, argb);
            return;
        }

        beginGuiBlend();
        try {
            // Main body. Two quads cover everything except the four corner caps.
            quad(left + r, top, right - r, bottom, argb);
            quad(left, top + r, left + r, bottom - r, argb);
            quad(right - r, top + r, right, bottom - r, argb);

            arc(left + r, top + r, r, 180.0D, 270.0D, argb);
            arc(right - r, top + r, r, 270.0D, 360.0D, argb);
            arc(right - r, bottom - r, r, 0.0D, 90.0D, argb);
            arc(left + r, bottom - r, r, 90.0D, 180.0D, argb);
        } finally {
            endGuiBlend();
        }
    }

    public static void rect(double left, double top, double right, double bottom, int argb) {
        if (right <= left || bottom <= top || ((argb >>> 24) & 255) == 0)
            return;
        beginGuiBlend();
        try {
            quad(left, top, right, bottom, argb);
        } finally {
            endGuiBlend();
        }
    }

    private static void beginGuiBlend() {
        // Mirror vanilla Gui#drawGradientRect state setup. This is deliberately
        // boring: legacy clients and wrappers agree on it, which is exactly what
        // a background primitive needs.
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(
            GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void endGuiBlend() {
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void quad(double left, double top, double right, double bottom, int argb) {
        int a = (argb >>> 24) & 255;
        int r = (argb >>> 16) & 255;
        int g = (argb >>> 8) & 255;
        int b = argb & 255;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(left, bottom, 0.0D).color(r, g, b, a).endVertex();
        wr.pos(right, bottom, 0.0D).color(r, g, b, a).endVertex();
        wr.pos(right, top, 0.0D).color(r, g, b, a).endVertex();
        wr.pos(left, top, 0.0D).color(r, g, b, a).endVertex();
        tessellator.draw();
    }

    private static void arc(double cx, double cy, double radius, double startDeg, double endDeg, int argb) {
        int a = (argb >>> 24) & 255;
        int r = (argb >>> 16) & 255;
        int g = (argb >>> 8) & 255;
        int b = argb & 255;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(cx, cy, 0.0D).color(r, g, b, a).endVertex();
        for (int i = 0; i <= CORNER_SEGMENTS; i++) {
            double angle = Math.toRadians(startDeg + (endDeg - startDeg) * i / CORNER_SEGMENTS);
            wr.pos(cx + Math.cos(angle) * radius, cy + Math.sin(angle) * radius, 0.0D)
                .color(r, g, b, a)
                .endVertex();
        }
        tessellator.draw();
    }
}
