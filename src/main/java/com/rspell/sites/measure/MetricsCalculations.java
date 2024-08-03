package com.rspell.sites.measure;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class MetricsCalculations {

    @Getter
    private Long totalSessions = 0L;
    public void increaseTotalSessions() {
        totalSessions++;
    }

    @Getter
    @Setter
    private Integer categoryCount = 0;

    // Percent Metrics
    @Getter
    private final Map<Metric, CategoryPercentMetric> percentMetrics = new LinkedHashMap<>();
    public void addPercentMetric(final Metric metricType, final CategoryPercentMetric metricCalc) {
        percentMetrics.put(metricType, metricCalc);
    }
    public CategoryPercentMetric getPercentMetric(final Metric metricType) {
        return percentMetrics.get(metricType);
    }

    // Category Click Metrics
    @Getter
    private final Map<String, CategoryClickMetric> categoryClickMetrics = new LinkedHashMap<>();
    public void addClickMetric(final String catName, final CategoryClickMetric metricCalc) {
        categoryClickMetrics.put(catName, metricCalc);
    }
    @Getter
    private final Set<Metric> clickMetricTypes = new LinkedHashSet<>();
    public void addClickMetricType(final Metric metricType) {
        clickMetricTypes.add(metricType);
    }

    // Category Time Metrics
    @Getter
    private final Map<String, CategoryTimeMetric> categoryTimeMetrics = new LinkedHashMap<>();
    @Getter
    private final Set<Metric> timeMetricTypes = new LinkedHashSet<>();
    public void addTimeMetricType(final Metric metricType) {
        timeMetricTypes.add(metricType);
    }
    public void addTimeMetric(final String catName, final CategoryTimeMetric metricCalc) {
        categoryTimeMetrics.put(catName, metricCalc);
    }
}
