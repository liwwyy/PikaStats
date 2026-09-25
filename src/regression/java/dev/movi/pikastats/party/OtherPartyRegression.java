package dev.movi.pikastats.party;

import java.util.HashMap;
import java.util.Map;

public final class OtherPartyRegression {
    private OtherPartyRegression() {}
    public static boolean run() {
        Map<String, Long> joins = new HashMap<String, Long>();
        joins.put("alice", 1000L);
        joins.put("bob", 1080L);
        joins.put("solo", 1400L);
        joins.put("carol", 2000L);
        joins.put("dave", 2090L);
        joins.put("erin", 3000L);
        joins.put("frank", 3090L);
        return OtherPartyDetector.groupIndex(joins, "alice", 150) == 0
            && OtherPartyDetector.groupIndex(joins, "bob", 150) == 0
            && OtherPartyDetector.groupIndex(joins, "solo", 150) == -1
            && OtherPartyDetector.groupIndex(joins, "carol", 150) == 1
            && OtherPartyDetector.groupIndex(joins, "dave", 150) == 1
            && OtherPartyDetector.groupIndex(joins, "erin", 150) == 2
            && OtherPartyDetector.groupIndex(joins, "frank", 150) == 2;
    }
}
