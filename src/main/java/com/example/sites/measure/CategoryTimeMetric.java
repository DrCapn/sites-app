package com.example.sites.measure;

import lombok.Builder;
import lombok.Getter;

@Builder
public class CategoryTimeMetric {
    @Getter
    private String categoryName;
    @Getter
    private Long totalTimes;
    @Getter
    private Long numTimes;

    public float getAverageTime() {
        if (totalTimes == null || totalTimes == 0) {
            return 0.0F;
        }
        if (numTimes == null || numTimes == 0) {
            return 0.0F;
        }
        return totalTimes.floatValue() / numTimes.floatValue();
    }
    public float getAverageTimeSec() {
        float avg_ms = getAverageTime();
        return avg_ms / 1000F;
    }
}
