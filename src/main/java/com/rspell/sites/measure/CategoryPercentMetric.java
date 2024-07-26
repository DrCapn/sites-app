package com.example.sites.measure;

import lombok.Builder;
import lombok.Getter;

@Builder
public class CategoryPercentMetric {
    private static final String PREFIX_INSTANCE = "%1$s %2$s";

    @Getter
    private Metric metric;
    @Getter
    private Long count;
    @Getter
    private String zeroText;
    @Getter
    private String allText;

    public void increaseCount() {
        if (count == null) count = 0L;
        count++;
    }
    public float getPercentage(Long total) {
        if (total == null || total == 0) {
            return 0.0F;
        }
        if (count == null || count == 0) {
            return 0.0F;
        }
        return (count.floatValue() / total.floatValue()) * 100;
    }
}
