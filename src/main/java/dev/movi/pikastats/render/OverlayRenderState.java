package dev.movi.pikastats.render;

import java.nio.FloatBuffer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

/**
 * Isolates overlay transforms and texture state from other HUDs and world
 * rendering.
 */
public final class OverlayRenderState implements AutoCloseable {
    private final int matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
    private final int activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    private final int program =
        GLContext.getCapabilities().OpenGL20 ? GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM) : 0;
    private final boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
    private final boolean alpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
    private final boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
    private final boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
    private final boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
    private final int src = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
    private final int dst = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
    private final int srcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
    private final int dstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
    private final FloatBuffer color = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer textureMatrix = BufferUtils.createFloatBuffer(16);
    private final int texture;
    private final boolean textured;

    public OverlayRenderState() {
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        textured = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        // Save matrices directly: legacy texture stacks can have a depth of only
        // two.
        GL11.glGetFloat(GL11.GL_TEXTURE_MATRIX, textureMatrix);
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        if (program != 0)
            GL20.glUseProgram(0);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.color(1, 1, 1, 1);
    }

    @Override
    public void close() {
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(modelView);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GL11.glLoadMatrix(textureMatrix);
        GlStateManager.bindTexture(texture);
        if (textured)
            GlStateManager.enableTexture2D();
        else
            GlStateManager.disableTexture2D();
        // A surrounding HUD mod may have pushed a matrix in another mode.
        // Return to that mode so its matching pop affects the same stack.
        GlStateManager.setActiveTexture(activeTexture);
        GlStateManager.matrixMode(matrixMode);
        GlStateManager.tryBlendFuncSeparate(src, dst, srcAlpha, dstAlpha);
        if (blend)
            GlStateManager.enableBlend();
        else
            GlStateManager.disableBlend();
        if (alpha)
            GlStateManager.enableAlpha();
        else
            GlStateManager.disableAlpha();
        if (depth)
            GlStateManager.enableDepth();
        else
            GlStateManager.disableDepth();
        if (lighting)
            GlStateManager.enableLighting();
        else
            GlStateManager.disableLighting();
        if (cull)
            GlStateManager.enableCull();
        else
            GlStateManager.disableCull();
        GlStateManager.color(color.get(0), color.get(1), color.get(2), color.get(3));
        if (program != 0)
            GL20.glUseProgram(program);
    }
}
