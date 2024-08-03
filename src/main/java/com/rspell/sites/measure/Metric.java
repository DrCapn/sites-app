package com.rspell.sites.measure;

public enum Metric {

    SESS_UNSEEN("Clicked Some", ""),
    SESS_SEEN_ALL("Clicked All", ""),
    SESS_SEEN_NONE("Clicked None", ""),
    CLICK_SESS_ALL("Click Count", ""),
    CLICK_SESS_FIRST("First View", ""),
    CLICK_SESS_HIT("Not-First Views", ""),
    CLICK_SESS_VIEWS("Page Views", ""),
    TIME_VIEWED("Avg. Time", "");

    public final String label;
    public final String description;
    Metric(String label, String description) {
        this.label = label;
        this.description = description;
    }
}
