package dev.movi.pikastats;

import dev.movi.pikastats.render.OverlayRenderState;
import dev.movi.pikastats.render.RenderUtil;
import java.nio.FloatBuffer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

/**
 * Optional real-driver test using an offscreen Pbuffer; requires an X display
 * and LWJGL natives.
 */
public final class RenderStateSmoke {
    public static void main(String[] args) throws Exception {
        Pbuffer buffer = new Pbuffer(
            64, 64, new PixelFormat().withBitsPerPixel(24).withAlphaBits(8).withDepthBits(24), null,
            null);
        try {
            buffer.makeCurrent();
            OpenGlHelper.defaultTexUnit = GL13.GL_TEXTURE0;
            OpenGlHelper.lightmapTexUnit = GL13.GL_TEXTURE1;
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
            int texture = GL11.glGenTextures();
            GlStateManager.bindTexture(texture);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            GlStateManager.translate(.25f, .5f, 0);
            int depth = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_STACK_DEPTH);
            for (int i = 1; i < depth; i++)
                GlStateManager.pushMatrix();
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.pushMatrix(); // surrounding optimization HUD
            int callerDepth = GL11.glGetInteger(GL11.GL_TEXTURE_STACK_DEPTH);
            GlStateManager.enableDepth();
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.color(.2f, .3f, .4f, .5f);
            for (int frame = 0; frame < 500; frame++) {
                try (OverlayRenderState state = new OverlayRenderState()) {
                    require(GL11.glGetInteger(GL11.GL_MATRIX_MODE) == GL11.GL_MODELVIEW,
                            "modelview selected");
                    require(GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE) == GL13.GL_TEXTURE0,
                            "texture zero selected");
                    GlStateManager.translate(frame, 2, 0);
                    GlStateManager.scale(.8f, .8f, 1);
                    RenderUtil.roundedRect(0, 0, 30, 20, 6, 0x80123456);
                    if ((frame & 1) != 0)
                        throw new ExpectedFailure();
                } catch (ExpectedFailure expected) {
                    // State must also be restored when a drawing operation fails.
                }
                require(GL11.glGetInteger(GL11.GL_MATRIX_MODE) == GL11.GL_TEXTURE,
                        "caller matrix mode restored");
                require(GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE) == GL13.GL_TEXTURE1,
                        "caller texture unit restored");
                require(GL11.glGetInteger(GL11.GL_TEXTURE_STACK_DEPTH) == callerDepth,
                        "caller matrix stack remains balanced");
                require(GL11.glIsEnabled(GL11.GL_DEPTH_TEST) &&
                            GL11.glIsEnabled(GL11.GL_LIGHTING) &&
                            GL11.glIsEnabled(GL11.GL_CULL_FACE) && !GL11.glIsEnabled(GL11.GL_BLEND),
                        "render flags restored");
                FloatBuffer color = BufferUtils.createFloatBuffer(16);
                GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);
                require(Math.abs(color.get(0) - .2f) < .001f &&
                            Math.abs(color.get(3) - .5f) < .001f,
                        "color restored");
                GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
                require(GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D) == texture,
                        "texture binding restored");
                require(GL11.glGetInteger(GL11.GL_TEXTURE_STACK_DEPTH) == depth,
                        "full texture stack unchanged");
                FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
                GL11.glGetFloat(GL11.GL_TEXTURE_MATRIX, matrix);
                require(matrix.get(12) == .25f && matrix.get(13) == .5f, "texture matrix restored");
                GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrix);
                require(matrix.get(0) == 1 && matrix.get(12) == 0, "modelview restored");
                GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                require(GL11.glGetError() == GL11.GL_NO_ERROR, "no GL errors on frame " + frame);
            }
            GlStateManager.popMatrix();
            require(GL11.glGetInteger(GL11.GL_TEXTURE_STACK_DEPTH) == callerDepth - 1,
                    "surrounding HUD can pop its own texture matrix");
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            try (dev.movi.pikastats.render.OverlayClip clip =
                     dev.movi.pikastats.render.OverlayClip.forDisplay(1, 64, 4, 5, 30, 20)) {
                require(GL11.glIsEnabled(GL11.GL_SCISSOR_TEST), "resize clipping enabled");
            }
            require(!GL11.glIsEnabled(GL11.GL_SCISSOR_TEST), "resize clipping disabled after draw");
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(2, 3, 40, 41);
            try (dev.movi.pikastats.render.OverlayClip clip =
                     dev.movi.pikastats.render.OverlayClip.forDisplay(1, 64, 4, 5, 30, 20)) {
                require(GL11.glIsEnabled(GL11.GL_SCISSOR_TEST), "caller scissor remains enabled");
            }
            java.nio.IntBuffer restoredScissor = BufferUtils.createIntBuffer(16);
            GL11.glGetInteger(GL11.GL_SCISSOR_BOX, restoredScissor);
            require(restoredScissor.get(0) == 2 && restoredScissor.get(1) == 3 &&
                        restoredScissor.get(2) == 40 && restoredScissor.get(3) == 41,
                    "caller scissor restored");
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            java.lang.reflect.Method initialize =
                dev.movi.pikastats.render.GlassBackground.class.getDeclaredMethod("initialize");
            initialize.setAccessible(true);
            initialize.invoke(null);
            require(GL11.glGetError() == GL11.GL_NO_ERROR, "glass shader compiles and links");
            Object fonts = dev.movi.pikastats.render.OverlayText.get();
            java.lang.reflect.Field customFont = fonts.getClass().getDeclaredField("font");
            customFont.setAccessible(true);
            customFont.set(fonts, java.awt.Font.decode("Dialog-PLAIN-14"));
            java.lang.reflect.Method raster =
                fonts.getClass().getDeclaredMethod("raster", String.class, int.class);
            raster.setAccessible(true);
            java.awt.image.BufferedImage rendered =
                (java.awt.image.BufferedImage)raster.invoke(fonts, "§cNICKED §7Ω", 0xffffffff);
            require(rendered.getWidth() > 30 && rendered.getHeight() > 10,
                    "formatted TTF raster dimensions");
            int opaque = 0;
            for (int py = 0; py < rendered.getHeight(); py++)
                for (int px = 0; px < rendered.getWidth(); px++)
                    if ((rendered.getRGB(px, py) >>> 24) != 0)
                        opaque++;
            require(opaque > 30, "formatted TTF raster contains glyph pixels");
            // TextureUtil consults only the client's anaglyph flag during upload.
            // Supply an inert client shell; the smoke test never starts Minecraft.
            Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field singleton = unsafeType.getDeclaredField("theUnsafe");
            singleton.setAccessible(true);
            Object unsafe = singleton.get(null);
            java.lang.reflect.Method allocate = unsafeType.getMethod("allocateInstance", Class.class);
            net.minecraft.client.Minecraft client = (net.minecraft.client.Minecraft)allocate.invoke(
                unsafe, net.minecraft.client.Minecraft.class);
            client.gameSettings = (net.minecraft.client.settings.GameSettings)allocate.invoke(
                unsafe, net.minecraft.client.settings.GameSettings.class);
            java.lang.reflect.Field clientField = net.minecraft.client.Minecraft.class.getDeclaredField("theMinecraft");
            clientField.setAccessible(true);
            clientField.set(null, client);
            net.minecraft.client.renderer.texture.DynamicTexture customTexture =
                new net.minecraft.client.renderer.texture.DynamicTexture(rendered);
            GlStateManager.bindTexture(customTexture.getGlTextureId());
            require(GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH) ==
                        rendered.getWidth(),
                    "custom TTF texture uploads to the driver");
            customTexture.deleteGlTexture();
            require(GL11.glGetError() == GL11.GL_NO_ERROR, "custom font upload has no GL error");
            java.nio.file.Path assets = java.nio.file.Paths.get("/tmp/pikastats-font-smoke/config/pikastats");
            java.nio.file.Files.createDirectories(assets);
            java.nio.file.Files.copy(java.nio.file.Paths.get("/usr/share/fonts/noto/NotoSans-Medium.ttf"),
                assets.resolve("custom.ttf"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            java.lang.reflect.Field dataDir = net.minecraft.client.Minecraft.class.getDeclaredField("mcDataDir");
            dataDir.setAccessible(true);
            dataDir.set(client, assets.getParent().getParent().toFile());
            dev.movi.pikastats.config.PikaConfig.fontMode = 2;
            dev.movi.pikastats.config.PikaConfig.customFont = "custom.ttf";
            dev.movi.pikastats.render.OverlayText.reload();
            dev.movi.pikastats.render.OverlayText custom = dev.movi.pikastats.render.OverlayText.get();
            require(custom.getStringWidth("§cNICKED §7Ω") > 30, "custom TTF width in formatted text");
            try (OverlayRenderState state = new OverlayRenderState()) {
                custom.drawStringWithShadow("§cNICKED §7Ω", 3, 4, 0xffffffff);
            }
            require(GL11.glGetError() == GL11.GL_NO_ERROR, "complete custom font draw has no GL error");
            dev.movi.pikastats.config.PikaConfig.fontMode = 1;
            dev.movi.pikastats.render.OverlayText.reload();
            require(custom.getStringWidth("§lPoppins §oΩ") > 20, "bundled Poppins loads and measures");
            try (OverlayRenderState state = new OverlayRenderState()) {
                custom.drawStringWithShadow("§lPoppins §oΩ", 3, 16, 0xffffffff);
            }
            require(GL11.glGetError() == GL11.GL_NO_ERROR, "bundled Poppins draw has no GL error");
            System.out.println("Passed 500 offscreen render-state frames, glass shader " +
                               "compilation, and custom TTF upload on " +
                               GL11.glGetString(GL11.GL_RENDERER));
        } finally {
            buffer.destroy();
        }
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new AssertionError(message);
    }
    private static final class ExpectedFailure extends RuntimeException {}
}
