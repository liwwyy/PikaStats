package dev.movi.pikastats.util;

import java.awt.Desktop;
import java.net.URI;
import org.apache.logging.log4j.LogManager;

public final class Links {
    private Links() {}
    public static void open(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            LogManager.getLogger("PikaStats").warn("Could not open " + url, e);
        }
    }
}
