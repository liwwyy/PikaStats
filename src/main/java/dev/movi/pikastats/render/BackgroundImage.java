package dev.movi.pikastats.render;

import dev.movi.pikastats.config.PikaConfig;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.apache.logging.log4j.LogManager;

/**
 * Optional assets are read off-thread; GPU uploads happen only on the render
 * thread.
 */
public final class BackgroundImage {
    private static final long CACHE_LIMIT = 8L * 1024 * 1024;
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "PikaStats-Images");
        t.setDaemon(true);
        return t;
    });
    public static final BackgroundImage TAB = new BackgroundImage(true);
    public static final BackgroundImage HUD = new BackgroundImage(false);
    private final boolean tab;
    private String requested = "";
    private volatile long generation;
    private volatile Loaded pending;
    private static final class Loaded {
        final long generation;
        final BufferedImage image;
        Loaded(long generation, BufferedImage image) {
            this.generation = generation;
            this.image = image;
        }
    }
    private DynamicTexture texture;
    private int width, height;
    private float lockedScale = -1f;
    private BackgroundImage(boolean tab) { this.tab = tab; }
    public static void shutdown() {
        IO.shutdownNow();
        try {
            IO.awaitTermination(15, TimeUnit.SECONDS);
            if (net.minecraft.client.Minecraft.getMinecraft() == null) return;
            Path cache = AssetFiles.directory().resolve("catbox-cache");
            clearCatboxCache(cache);
        } catch (IOException | InterruptedException e) {
            LogManager.getLogger("PikaStats").warn("Could not clear Catbox cache on exit", e);
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
        }
    }
    static void clearCatboxCache(Path cache) throws IOException {
        if (!Files.isDirectory(cache)) return;
        try (DirectoryStream<Path> files = Files.newDirectoryStream(cache)) {
            for (Path file : files) {
                String name = file.getFileName().toString();
                if ((name.matches("[A-Za-z0-9_-]+\\.png") || "last-image.txt".equals(name))
                    && !Files.isSymbolicLink(file)) Files.deleteIfExists(file);
            }
        }
    }
    public static void reload() {
        TAB.invalidate();
        HUD.invalidate();
    }
    private synchronized void invalidate() {
        requested = "";
        generation++;
        pending = null;
        lockedScale = -1f;
    }
    public void requestNewWaifu() {
        if (tab) {
            PikaConfig.tabRandomWaifu = true;
            PikaConfig.tabRandomBackground = false;
        } else {
            PikaConfig.hudRandomWaifu = true;
            PikaConfig.hudRandomBackground = false;
        }
        invalidate();
        PikaConfig.persist();
    }
    public void draw(float left, float top, float right, float bottom) {
        if (!(tab ? PikaConfig.tabImageEnabled : PikaConfig.hudImageEnabled) &&
            !(tab ? PikaConfig.tabRandomWaifu : PikaConfig.hudRandomWaifu) &&
            !(tab ? PikaConfig.tabRandomBackground : PikaConfig.hudRandomBackground))
            return;
        if (PikaConfig.lowPerformanceMode) return;
        final boolean folderRandom = tab ? PikaConfig.tabRandomBackground : PikaConfig.hudRandomBackground;
        final boolean random = (tab ? PikaConfig.tabRandomWaifu : PikaConfig.hudRandomWaifu);
        final String filename = (tab ? PikaConfig.tabImageFile : PikaConfig.hudImageFile);
        String key = folderRandom ? "backgrounds:random" : random ? "catbox" : String.valueOf(filename);
        if (!key.equals(requested)) {
            requested = key;
            lockedScale = -1f;
            final long token = ++generation;
            pending = null;
            if (texture != null) {
                texture.deleteGlTexture();
                texture = null;
            }
            IO.submit(() -> {
                try {
                    BufferedImage image =
                        folderRandom ? randomLocalImage() : random ? randomImage()
                            : decode(Files.readAllBytes(checkedLocal(filename)));
                    if (token == generation)
                        pending = new Loaded(token, image);
                } catch (Exception e) {
                    LogManager.getLogger("PikaStats")
                        .warn("Background image unavailable; keeping the base panel", e);
                }
            });
        }
        Loaded loaded = pending;
        if (loaded != null) {
            pending = null;
            if (loaded.generation == generation) {
                BufferedImage image = loaded.image;
                if (texture != null)
                    texture.deleteGlTexture();
                texture = new DynamicTexture(image);
                width = image.getWidth();
                height = image.getHeight();
                lockedScale = -1f;
            }
        }
        if (texture == null)
            return;
        float availableW = Math.max(0, right - left - 12), availableH =
                                                               Math.max(0, bottom - top - 12);
        float scale = Math.min(availableW / width, availableH / height);
        scale *= Math.max(
            .1f, Math.min(2f, (tab ? PikaConfig.tabImageSize : PikaConfig.hudImageSize) / 100f));
        if (PikaConfig.staticImageSize) {
            if (lockedScale < 0f) lockedScale = scale;
            scale = lockedScale;
        } else {
            lockedScale = -1f;
        }
        float w = width * scale, h = height * scale;
        int position = tab ? PikaConfig.tabImagePosition : PikaConfig.hudImagePosition;
        float x = position == 1   ? (left + right - w) / 2f
                  : position == 2 ? right - 6 - w
                                  : left + 6,
              y = top + 6 + (availableH - h) / 2;
        try {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.disableAlpha();
            GlStateManager.color(1, 1, 1,
                                 Math.max(.1f, Math.min(1, (tab ? PikaConfig.tabImageOpacity
                                                                : PikaConfig.hudImageOpacity) /
                                                               100f)));
            GlStateManager.bindTexture(texture.getGlTextureId());
            net.minecraft.client.renderer.Tessellator tess =
                net.minecraft.client.renderer.Tessellator.getInstance();
            net.minecraft.client.renderer.WorldRenderer wr = tess.getWorldRenderer();
            wr.begin(org.lwjgl.opengl.GL11.GL_QUADS,
                     net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
            wr.pos(x, y + h, 0).tex(0, 1).endVertex();
            wr.pos(x + w, y + h, 0).tex(1, 1).endVertex();
            wr.pos(x + w, y, 0).tex(1, 0).endVertex();
            wr.pos(x, y, 0).tex(0, 0).endVertex();
            tess.draw();
        } finally {
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }
    }
    private static Path checkedLocal(String filename) throws IOException {
        Path p = AssetFiles.localWithLegacy("backgrounds", filename);
        if (Files.size(p) > CACHE_LIMIT)
            throw new IOException("Image exceeds 8 MiB");
        return p;
    }
    private static BufferedImage randomLocalImage() throws IOException {
        Path folder = AssetFiles.folder("backgrounds");
        List<Path> images = new ArrayList<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(folder)) {
            for (Path file : files)
                if (Files.isRegularFile(file) && !Files.isSymbolicLink(file) &&
                    file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png") &&
                    Files.size(file) <= CACHE_LIMIT)
                    images.add(file);
        }
        if (images.isEmpty()) throw new IOException("No PNG images in config/pikastats/backgrounds");
        Collections.shuffle(images);
        IOException failure = new IOException("No usable PNG images in config/pikastats/backgrounds");
        for (Path file : images) {
            try { return decode(Files.readAllBytes(file)); }
            catch (IOException ex) { failure = ex; }
        }
        throw failure;
    }
    private static BufferedImage decode(byte[] data) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext())
                throw new IOException("Unsupported image");
            ImageReader reader = readers.next();
            try {
                reader.setInput(in);
                int w = reader.getWidth(0), h = reader.getHeight(0);
                if (w < 1 || h < 1 || w > 4096 || h > 4096 || (long)w * h > 8_000_000)
                    throw new IOException("Image dimensions exceed limit");
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        }
    }
    private BufferedImage randomImage() throws IOException {
        Path cache = AssetFiles.directory().resolve("catbox-cache");
        Files.createDirectories(cache);
        Path lastFile = cache.resolve("last-image.txt");
        String last = Files.exists(lastFile)
                          ? new String(Files.readAllBytes(lastFile), StandardCharsets.UTF_8)
                          : "";
        List<String> names = new ArrayList<>();
        try {
            String script = new String(download("https://catbox.moe/resources/pic.js", 512 * 1024),
                                       StandardCharsets.UTF_8);
            Matcher matcher = Pattern.compile("\"([A-Za-z0-9_-]+\\.png)\"").matcher(script);
            while (matcher.find())
                if (!names.contains(matcher.group(1)))
                    names.add(matcher.group(1));
        } catch (IOException e) {
            LogManager.getLogger("PikaStats").debug("Catbox manifest unavailable; using cache", e);
        }
        try (DirectoryStream<Path> files = Files.newDirectoryStream(cache, "*.png")) {
            for (Path p : files)
                if (!names.contains(p.getFileName().toString()))
                    names.add(p.getFileName().toString());
        }
        Collections.shuffle(names);
        names.remove(last);
        if (!last.isEmpty())
            names.add(last);
        IOException failure = new IOException("No Catbox images available");
        for (String name : names) {
            if (!name.matches("[A-Za-z0-9_-]+\\.png"))
                continue;
            Path file = cache.resolve(name);
            try {
                byte[] data =
                    Files.exists(file) && Files.size(file) <= CACHE_LIMIT
                        ? Files.readAllBytes(file)
                        : download("https://catbox.moe/pictures/qts/" + name, CACHE_LIMIT);
                BufferedImage image = decode(data);
                if (!Files.exists(file)) {
                    trim(cache, data.length);
                    Files.write(file, data);
                }
                Files.write(lastFile, name.getBytes(StandardCharsets.UTF_8));
                return image;
            } catch (IOException e) {
                failure = e;
            }
        }
        throw failure;
    }
    private static void trim(Path cache, long incoming) throws IOException {
        List<Path> files = new ArrayList<>();
        long total = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(cache, "*.png")) {
            for (Path p : stream) {
                files.add(p);
                total += Files.size(p);
            }
        }
        files.sort(Comparator.comparingLong(p -> p.toFile().lastModified()));
        for (Path p : files) {
            if (total + incoming <= CACHE_LIMIT)
                break;
            long size = Files.size(p);
            Files.delete(p);
            total -= size;
        }
    }
    private static byte[] download(String address, long limit) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(8000);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", "PikaStats/1.2");
        try {
            if (connection.getResponseCode() != 200)
                throw new IOException("Image provider HTTP " + connection.getResponseCode());
            if (connection.getContentLengthLong() > limit)
                throw new IOException("Download exceeds limit");
            try (InputStream in = connection.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) != -1) {
                    if (Thread.currentThread().isInterrupted())
                        throw new InterruptedIOException();
                    if (out.size() + n > limit)
                        throw new IOException("Download exceeds limit");
                    out.write(buffer, 0, n);
                }
                return out.toByteArray();
            }
        } finally {
            connection.disconnect();
        }
    }
}
