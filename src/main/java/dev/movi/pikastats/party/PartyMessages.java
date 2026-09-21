package dev.movi.pikastats.party;
import java.util.regex.Pattern;
public final class PartyMessages {
    private PartyMessages() {}
    private static final Pattern DISBAND =
        Pattern.compile("^Party\\s*▏\\s*(?:The party has been disbanded|[A-Za-z0-9_]{3,16}\\s+disbanded the "
                        + "party)!?(?:\\s|$)",
            Pattern.CASE_INSENSITIVE);
    public static boolean isDisband(String text) {
        return DISBAND.matcher(text).find();
    }
}
