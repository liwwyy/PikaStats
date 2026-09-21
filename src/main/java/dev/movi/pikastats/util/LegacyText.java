package dev.movi.pikastats.util;

import java.util.regex.Pattern;
import net.minecraft.util.EnumChatFormatting;

public final class LegacyText {
    private static final Pattern STRIP = Pattern.compile("(?i)\\u00a7[0-9A-FK-OR]");
    private LegacyText() {}
    public static String ampToSection(String s) {
        return s == null ? "" : s.replaceAll("(?i)&([0-9A-FK-OR])", "§$1");
    }
    public static String plain(String s) {
        return s == null ? "" : STRIP.matcher(ampToSection(s)).replaceAll("");
    }
    public static EnumChatFormatting firstColor(String raw, EnumChatFormatting fallback) {
        String s = ampToSection(raw);
        for (int i = 0; i + 1 < s.length(); i++)
            if (s.charAt(i) == '§') {
                EnumChatFormatting f =
                    EnumChatFormatting.getValueByName(codeName(Character.toLowerCase(s.charAt(i + 1))));
                if (f != null && f.isColor())
                    return f;
            }
        return fallback;
    }
    private static String codeName(char c) {
        switch (c) {
            case '0':
                return "black";
            case '1':
                return "dark_blue";
            case '2':
                return "dark_green";
            case '3':
                return "dark_aqua";
            case '4':
                return "dark_red";
            case '5':
                return "dark_purple";
            case '6':
                return "gold";
            case '7':
                return "gray";
            case '8':
                return "dark_gray";
            case '9':
                return "blue";
            case 'a':
                return "green";
            case 'b':
                return "aqua";
            case 'c':
                return "red";
            case 'd':
                return "light_purple";
            case 'e':
                return "yellow";
            case 'f':
                return "white";
            default:
                return "";
        }
    }
}
