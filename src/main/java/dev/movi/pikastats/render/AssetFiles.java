package dev.movi.pikastats.render;

import java.io.IOException;
import java.nio.file.*;
import net.minecraft.client.Minecraft;

public final class AssetFiles {
    private AssetFiles() {}
    public static void prepare() throws IOException {
        folder("backgrounds");
        folder("fonts");
    }
    public static Path directory() throws IOException {
        Path root = Minecraft.getMinecraft().mcDataDir.toPath().resolve("config/pikastats");
        Files.createDirectories(root);
        return root.toRealPath();
    }
    public static Path local(String name) throws IOException {
        Path root = directory();
        if (name == null || name.trim().isEmpty())
            throw new IOException("Empty asset filename");
        Path file = root.resolve(name).normalize();
        if (!file.startsWith(root) || !file.toRealPath().startsWith(root))
            throw new IOException("Asset must be inside config/pikastats");
        return file;
    }
    public static Path folder(String name) throws IOException {
        Path dir = directory().resolve(name);
        Files.createDirectories(dir);
        return dir.toRealPath();
    }
    public static Path localIn(String folder, String name) throws IOException {
        if (name == null || name.trim().isEmpty())
            throw new IOException("Empty asset filename");
        Path root = folder(folder);
        Path file = root.resolve(name).normalize();
        if (!file.startsWith(root) || !file.toRealPath().startsWith(root))
            throw new IOException("Asset must be inside config/pikastats/" + folder);
        return file;
    }
    public static Path localWithLegacy(String folder, String name) throws IOException {
        try {
            return localIn(folder, name);
        } catch (NoSuchFileException missing) {
            return local(name);
        }
    }
}
