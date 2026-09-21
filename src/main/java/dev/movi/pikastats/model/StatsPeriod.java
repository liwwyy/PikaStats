package dev.movi.pikastats.model;

public enum StatsPeriod {
    LIFETIME("total", "Lifetime"),
    WEEKLY("weekly", "Weekly"),
    MONTHLY("monthly", "Monthly"),
    YEARLY("yearly", "Yearly");
    public final String apiName;
    public final String label;
    StatsPeriod(String apiName, String label) {
        this.apiName = apiName;
        this.label = label;
    }
    public static StatsPeriod fromIndex(int i) {
        StatsPeriod[] v = values();
        return i >= 0 && i < v.length ? v[i] : LIFETIME;
    }
}
