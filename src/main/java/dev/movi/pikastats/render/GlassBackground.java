package dev.movi.pikastats.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.*;

/** Local frosted-glass shader; never installs/replaces the world's shader group. */
public final class GlassBackground {
    private static int program, texture, width, height;
    private static boolean unavailable;
    private GlassBackground() {}
    private static int shader(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            String log = GL20.glGetShaderInfoLog(shader, 2048);
            GL20.glDeleteShader(shader);
            throw new IllegalStateException(log);
        }
        return shader;
    }
    private static String source(String file) {
        try (java.io.InputStream in =
                 GlassBackground.class.getResourceAsStream("/assets/pikastats/shaders/" + file)) {
            if (in == null)
                throw new java.io.IOException("Missing shader: " + file);
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int length;
            while ((length = in.read(buffer)) != -1) out.write(buffer, 0, length);
            return new String(out.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Cannot read glass shader", e);
        }
    }
    private static void initialize() {
        int vertex = shader(GL20.GL_VERTEX_SHADER, source("glass.vert"));
        int fragment = 0;
        try {
            fragment = shader(GL20.GL_FRAGMENT_SHADER, source("glass.frag"));
            program = GL20.glCreateProgram();
            GL20.glAttachShader(program, vertex);
            GL20.glAttachShader(program, fragment);
            GL20.glLinkProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == 0)
                throw new IllegalStateException(GL20.glGetProgramInfoLog(program, 2048));
            texture = GL11.glGenTextures();
        } finally {
            GL20.glDeleteShader(vertex);
            if (fragment != 0)
                GL20.glDeleteShader(fragment);
        }
    }
    public static boolean draw(float l, float t, float r, float b, int opacity) {
        if (opacity <= 0)
            return true;
        if (unavailable || !GLContext.getCapabilities().OpenGL20 || !OpenGlHelper.isFramebufferEnabled())
            return false;
        int previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM),
            previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND), alpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        int src = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB), dst = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int srcA = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),
            dstA = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        try {
            if (program == 0)
                initialize();
            Minecraft mc = Minecraft.getMinecraft();
            GlStateManager.bindTexture(texture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            if (width != mc.displayWidth || height != mc.displayHeight) {
                width = mc.displayWidth;
                height = mc.displayHeight;
                GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 0, 0, width, height, 0);
            } else
                GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
            GL20.glUseProgram(program);
            GL20.glUniform1i(GL20.glGetUniformLocation(program, "scene"), 0);
            GL20.glUniform2f(GL20.glGetUniformLocation(program, "screen"), width, height);
            GL20.glUniform2f(GL20.glGetUniformLocation(program, "size"), r - l, b - t);
            GL20.glUniform1f(
                GL20.glGetUniformLocation(program, "opacity"), Math.max(0, Math.min(1, opacity / 100f)));
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 0);
            GL11.glVertex2f(l, t);
            GL11.glTexCoord2f(r - l, 0);
            GL11.glVertex2f(r, t);
            GL11.glTexCoord2f(r - l, b - t);
            GL11.glVertex2f(r, b);
            GL11.glTexCoord2f(0, b - t);
            GL11.glVertex2f(l, b);
            GL11.glEnd();
            return true;
        } catch (RuntimeException e) {
            unavailable = true;
            LogManager.getLogger("PikaStats").warn("Glass unavailable; using transparent panels", e);
            return false;
        } finally {
            GL20.glUseProgram(previousProgram);
            GlStateManager.bindTexture(previousTexture);
            GlStateManager.tryBlendFuncSeparate(src, dst, srcA, dstA);
            if (blend)
                GlStateManager.enableBlend();
            else
                GlStateManager.disableBlend();
            if (alpha)
                GlStateManager.enableAlpha();
            else
                GlStateManager.disableAlpha();
            GlStateManager.color(1, 1, 1, 1);
        }
    }
}
