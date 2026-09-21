package dev.movi.pikastats.model;

public enum TabSortStat {
    FKDR,
    WLR,
    BEST_WINSTREAK,
    FINAL_KILLS,
    WINS,
    BEDS,
    LEVEL;
    public static TabSortStat fromIndex(int i) {
        TabSortStat[] v = values();
        return i >= 0 && i < v.length ? v[i] : FKDR;
    }
}
