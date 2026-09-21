package dev.movi.pikastats.util;
import net.minecraft.util.EnumChatFormatting;
public final class PingColors {
    private PingColors() {}
    public static EnumChatFormatting formatting(int ms) {
        if (ms < 0)
            return EnumChatFormatting.GRAY;
        if (ms <= 90)
            return EnumChatFormatting.GREEN;
        if (ms <= 140)
            return EnumChatFormatting.YELLOW;
        if (ms <= 200)
            return EnumChatFormatting.GOLD;
        if (ms < 400)
            return EnumChatFormatting.RED;
        return EnumChatFormatting.DARK_RED;
    }
}
