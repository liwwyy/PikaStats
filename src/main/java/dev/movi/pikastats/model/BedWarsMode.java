package dev.movi.pikastats.model;

public enum BedWarsMode {
    OVERALL("ALL_MODES", "Overall"),
    SOLO("SOLO", "Solo"),
    DOUBLES("DOUBLES", "Doubles"),
    QUADS("QUAD", "Quads");
    public final String apiName;
    public final String label;
    BedWarsMode(String apiName, String label) {
        this.apiName = apiName;
        this.label = label;
    }
    public static BedWarsMode fromIndex(int i) {
        BedWarsMode[] v = values();
        return i >= 0 && i < v.length ? v[i] : OVERALL;
    }
}
