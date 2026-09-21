package dev.movi.pikastats.hud;

import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import dev.movi.pikastats.config.PikaConfig;
import net.minecraft.client.gui.GuiScreen;
import org.apache.logging.log4j.LogManager;

public final class HudEditor {
    private HudEditor() {}
    public static void open() {
        try {
            // HudGui is shipped at runtime, but intentionally omitted from OneConfig's API jar.
            GuiScreen editor = (GuiScreen) Class.forName("cc.polyfrost.oneconfig.internal.gui.HudGui")
                                   .getDeclaredConstructor()
                                   .newInstance();
            GuiUtils.displayScreen(editor);
        } catch (ReflectiveOperationException e) {
            LogManager.getLogger("PikaStats").error("Could not open OneConfig's HUD editor", e);
            PikaConfig.openOneConfig();
        }
    }
}
