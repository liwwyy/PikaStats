package dev.movi.pikastats.party;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

public final class PartyRegression {
    private PartyRegression() {}
    public static boolean run() throws Exception {
        Pattern join = pattern("JOIN"), selfJoin = pattern("SELF_JOIN"), leave = pattern("LEAVE"),
            selfLeave = pattern("SELF_LEAVE");
        return join.matcher("Party ▏ ✚ [VIP] Player123 joined the party!").find()
            && join.matcher("[VIP] Player123 joined the party!").find()
            && selfJoin.matcher("You have joined [VIP] Player123's party!").find()
            && leave.matcher("Party ▏ ▬ [VIP] Player123 left the party!").find()
            && selfLeave.matcher("You are not in a party.").find()
            && !join.matcher("Player123: joined the party!").find();
    }
    private static Pattern pattern(String name) throws Exception {
        Field field = PartyTracker.class.getDeclaredField(name);
        field.setAccessible(true);
        return (Pattern)field.get(null);
    }
}
