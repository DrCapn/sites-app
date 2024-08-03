package com.rspell.sites.measure;

import lombok.Builder;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Builder
public class CategoryClickMetric {

    @Getter
    private String categoryName;

    @Getter
    private Map<Metric, Double> metricCounts;

    public void addMetricCount(final Metric metricType, final Double count) {
        if (metricCounts == null) {
            metricCounts = new LinkedHashMap<>(4);
        }
        metricCounts.put(metricType, count);
    }
}
