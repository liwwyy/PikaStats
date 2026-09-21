package dev.movi.pikastats.render;

import java.nio.IntBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** Clips animated table contents without changing a caller's scissor state. */
public final class OverlayClip implements AutoCloseable {
    private final boolean enabled;
    private final boolean previous;
    private final int[] box = new int[4];

    private OverlayClip(int factor, int displayHeight, float left, float top,
                        float width, float height, boolean enabled) {
        this.enabled = enabled;
        if (!enabled) { previous = false; return; }
        previous = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        if (previous) {
            // LWJGL 2 requires room for 16 values even for a four-value query.
            IntBuffer saved = BufferUtils.createIntBuffer(16);
            GL11.glGetInteger(GL11.GL_SCISSOR_BOX, saved);
            for (int i = 0; i < 4; i++) box[i] = saved.get(i);
        }
        int x = Math.round(left * factor);
        int y = displayHeight - Math.round((top + height) * factor);
        int right = x + Math.max(0, Math.round(width * factor));
        int upper = y + Math.max(0, Math.round(height * factor));
        if (previous) {
            x = Math.max(x, box[0]);
            y = Math.max(y, box[1]);
            right = Math.min(right, box[0] + box[2]);
            upper = Math.min(upper, box[1] + box[3]);
        }
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, Math.max(0, right - x), Math.max(0, upper - y));
    }

    public static OverlayClip panel(float left, float top, float width, float height,
                                    boolean enabled) {
        if (!enabled) return new OverlayClip(1, 1, left, top, width, height, false);
        Minecraft mc = Minecraft.getMinecraft();
        return new OverlayClip(new ScaledResolution(mc).getScaleFactor(), mc.displayHeight,
                               left, top, width, height, true);
    }

    public static OverlayClip forDisplay(int factor, int displayHeight, float left, float top,
                                         float width, float height) {
        return new OverlayClip(factor, displayHeight, left, top, width, height, true);
    }

    @Override public void close() {
        if (!enabled) return;
        if (previous) GL11.glScissor(box[0], box[1], box[2], box[3]);
        else GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
