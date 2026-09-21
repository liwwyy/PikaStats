package dev.movi.pikastats.render;

import java.nio.file.Files;
import java.nio.file.Path;

public final class CatboxCacheRegression {
    private CatboxCacheRegression() {}

    public static boolean run() throws Exception {
        Path cache = Files.createTempDirectory("pikastats-catbox-check");
        Path image = cache.resolve("sample.png");
        Path marker = cache.resolve("last-image.txt");
        Path unrelated = cache.resolve("notes.txt");
        try {
            Files.write(image, new byte[] {1});
            Files.write(marker, new byte[] {2});
            Files.write(unrelated, new byte[] {3});
            BackgroundImage.clearCatboxCache(cache);
            return !Files.exists(image) && !Files.exists(marker) && Files.exists(unrelated);
        } finally {
            Files.deleteIfExists(image);
            Files.deleteIfExists(marker);
            Files.deleteIfExists(unrelated);
            Files.deleteIfExists(cache);
        }
    }
}
