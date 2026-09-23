package dev.movi.pikastats.render;

import dev.movi.pikastats.config.PikaConfig;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.BufferUtils;

/** Optional TTF rasterizer. Measurements do not touch OpenGL; uploads happen while drawing. */
public final class OverlayText {
    private static final OverlayText INSTANCE = new OverlayText();
    // Raster at twice the display resolution, then filter down on the GPU.
    private static final float SCALE = .36f;
    private static final long MAX_PIXELS = 4_000_000;
    private static final int[] CHAT_COLORS = {
        0x000000, 0x0000aa, 0x00aa00, 0x00aaaa, 0xaa0000, 0xaa00aa, 0xffaa00, 0xaaaaaa,
        0x555555, 0x5555ff, 0x55ff55, 0x55ffff, 0xff5555, 0xff55ff, 0xffff55, 0xffffff};
    private final Map<String, Entry> cache = new LinkedHashMap<String, Entry>(32, .75f, true);
    private long pixels;
    private String key = "";
    private Font font;
    private Font[] bundled;
    private boolean cacheDirty;
    private static final class Entry {
        final DynamicTexture texture;
        final int width, height;
        Entry(BufferedImage image) {
            width = image.getWidth();
            height = image.getHeight();
            texture = new DynamicTexture(image);
        }
        void delete() { texture.deleteGlTexture(); }
    }
    private OverlayText() {}
    public static OverlayText get() { return INSTANCE; }
    public static void reload() {
        INSTANCE.key = "";
        INSTANCE.cacheDirty = true;
    }
    private FontRenderer vanilla() { return Minecraft.getMinecraft().fontRendererObj; }
    private void ensure() {
        String next = (PikaConfig.lowPerformanceMode ? 0 : PikaConfig.fontMode) + ":" + PikaConfig.customFont;
        if (next.equals(key))
            return;
        key = next;
        cacheDirty = true;
        font = null;
        bundled = null;
        if (PikaConfig.lowPerformanceMode || PikaConfig.fontMode == 0) return;
        try {
            if (PikaConfig.fontMode == 1) {
                String[] names = {"Poppins-Regular.ttf", "Poppins-Bold.ttf",
                    "Poppins-Italic.ttf", "Poppins-BoldItalic.ttf"};
                Font[] loaded = new Font[names.length];
                for (int i = 0; i < names.length; i++) {
                    try (InputStream in = OverlayText.class.getResourceAsStream(
                            "/assets/pikastats/fonts/" + names[i])) {
                        if (in == null) throw new IllegalStateException("Missing bundled " + names[i]);
                        loaded[i] = Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(28f);
                    }
                }
                bundled = loaded;
                font = loaded[0];
            } else if (PikaConfig.fontMode == 2) {
                Path file = AssetFiles.localWithLegacy("fonts", PikaConfig.customFont);
                if (Files.size(file) > 8 * 1024 * 1024)
                    throw new IllegalArgumentException("TTF exceeds 8 MiB");
                try (InputStream in = Files.newInputStream(file)) {
                    font = Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(28f);
                }
            }
        } catch (Exception ex) {
            LogManager.getLogger("PikaStats").warn("Font unavailable; using Minecraft font", ex);
            font = null;
            bundled = null;
        }
    }
    public int getStringWidth(String value) {
        ensure();
        if (font == null)
            return vanilla().getStringWidth(value);
        if (value == null || value.isEmpty())
            return 0;
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            return Math.round(measure(value, g) * SCALE);
        } finally {
            g.dispose();
        }
    }
    public float lineHeight() {
        ensure();
        if (font == null) return vanilla().FONT_HEIGHT;
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            return g.getFontMetrics(font).getHeight() * SCALE;
        } finally {
            g.dispose();
        }
    }

    /** Center one formatted line inside a cell, shrinking only when needed. */
    public void drawFitted(String value, float left, float top, float width, float height,
                           boolean right) {
        drawFitted(value, left, top, width, height, right, .5f);
    }

    /** Center painted text in a standalone box without the cell baseline offset. */
    public void drawCenteredFitted(String value, float left, float top, float width, float height) {
        drawFitted(value, left, top, width, height, false, 0f);
    }

    private void drawFitted(String value, float left, float top, float width, float height,
                            boolean right, float baselineOffset) {
        if (value == null || value.isEmpty() || width <= 0 || height <= 0) return;
        float textWidth = Math.max(1, getStringWidth(value));
        float textHeight = Math.max(1, lineHeight());
        float fit = Math.min(1f, Math.min(width / textWidth, height / textHeight));
        float x = right ? left + width - textWidth * fit : left;
        float y = top + (height - textHeight * fit) / 2f + baselineOffset;
        if (fit >= .999f) {
            drawStringWithShadow(value, x, y, 0xFFFFFFFF);
            return;
        }
        FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrix);
        try {
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(fit, fit, 1f);
            drawStringWithShadow(value, 0, 0, 0xFFFFFFFF);
        } finally {
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadMatrix(matrix);
        }
    }
    private int measure(String value, Graphics2D g) {
        int width = 0;
        boolean bold = false, italic = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '§' && i + 1 < value.length()) {
                char code = Character.toLowerCase(value.charAt(++i));
                if (code == 'l')
                    bold = true;
                else if (code == 'o')
                    italic = true;
                else if ("0123456789abcdefr".indexOf(code) >= 0)
                    bold = italic = false;
                continue;
            }
            FontMetrics metrics = g.getFontMetrics(derive(bold, italic));
            width += Math.max(1, metrics.charWidth(c));
        }
        return width;
    }
    private Font derive(boolean bold, boolean italic) {
        if (bundled != null) return bundled[(bold ? 1 : 0) | (italic ? 2 : 0)];
        return font.deriveFont((bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0));
    }
    public void drawStringWithShadow(String value, float x, float y, int color) {
        ensure();
        if (font == null) {
            vanilla().drawStringWithShadow(value, x, y, color);
            return;
        }
        if (value == null || value.isEmpty())
            return;
        if (cacheDirty) {
            clear();
            cacheDirty = false;
        }
        String cacheKey = key + ":" + color + ":" + value;
        Entry entry = cache.get(cacheKey);
        if (entry == null) {
            // Deletion and upload stay on the render thread, never in getStringWidth().
            entry = new Entry(raster(value, color));
            cache.put(cacheKey, entry);
            pixels += (long)entry.width * entry.height;
            while (pixels > MAX_PIXELS || cache.size() > 256) {
                String oldest = cache.keySet().iterator().next();
                Entry removed = cache.remove(oldest);
                pixels -= (long)removed.width * removed.height;
                removed.delete();
            }
        }
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.bindTexture(entry.texture.getGlTextureId());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        float w = entry.width * SCALE, h = entry.height * SCALE;
        net.minecraft.client.renderer.Tessellator tess =
            net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_QUADS,
                 net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + h, 0).tex(0, 1).endVertex();
        wr.pos(x + w, y + h, 0).tex(1, 1).endVertex();
        wr.pos(x + w, y, 0).tex(1, 0).endVertex();
        wr.pos(x, y, 0).tex(0, 0).endVertex();
        tess.draw();
    }
    private void clear() {
        for (Entry e : cache.values())
            e.delete();
        cache.clear();
        pixels = 0;
    }
    private BufferedImage raster(String value, int baseColor) {
        BufferedImage scratch = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scratch.createGraphics();
        int width = Math.max(1, measure(value, g));
        int ascent = g.getFontMetrics(font).getAscent();
        int height = g.getFontMetrics(font).getHeight() + 3;
        g.dispose();
        BufferedImage image = new BufferedImage(width + 3, height, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int rgb = baseColor & 0xffffff;
        int alpha = (baseColor >>> 24) & 255;
        boolean bold = false, italic = false;
        int x = 1;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '§' && i + 1 < value.length()) {
                char code = Character.toLowerCase(value.charAt(++i));
                if (code == 'l')
                    bold = true;
                else if (code == 'o')
                    italic = true;
                else if (code == 'r') {
                    rgb = baseColor & 0xffffff;
                    bold = italic = false;
                } else if ("0123456789abcdef".indexOf(code) >= 0) {
                    rgb = CHAT_COLORS["0123456789abcdef".indexOf(code)];
                    bold = italic = false;
                }
                continue;
            }
            Font current = derive(bold, italic);
            g.setFont(current);
            g.setColor(
                new java.awt.Color((rgb >> 18) & 63, (rgb >> 10) & 63, (rgb >> 2) & 63, alpha));
            g.drawString(String.valueOf(c), x + 1, ascent + 2);
            g.setColor(new java.awt.Color((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, alpha));
            g.drawString(String.valueOf(c), x, ascent + 1);
            x += Math.max(1, g.getFontMetrics(current).charWidth(c));
        }
        g.dispose();
        return image;
    }
    public List<String> listFormattedStringToWidth(String value, int max) {
        ensure();
        if (font == null)
            return vanilla().listFormattedStringToWidth(value, max);
        List<String> lines = new ArrayList<String>();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '§' && i + 1 < value.length()) {
                line.append(c).append(value.charAt(++i));
                continue;
            }
            if (c == '\n' || (getStringWidth(line.toString() + c) > max &&
                              getStringWidth(line.toString()) > 0)) {
                String previous = line.toString();
                lines.add(previous);
                line = new StringBuilder(FontRenderer.getFormatFromString(previous));
                if (c == '\n')
                    continue;
            }
            line.append(c);
        }
        if (line.length() > 0)
            lines.add(line.toString());
        return lines;
    }
}
