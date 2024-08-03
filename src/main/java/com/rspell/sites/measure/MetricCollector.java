package com.rspell.sites.measure;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class MetricCollector {

    private static final Logger log = LoggerFactory.getLogger(MetricCollector.class);

    public static final String VIEWS_TAG = "views";
    public static final String SESSION_FIRST_VAL = "session_first";
    public static final String SESSION_HIT_VAL = "session_other";
    public static final String SESSION_ALL_VAL = "session_all";
    public static final String PAGE_VIEW_ALL = "page_all";

    private final MeterRegistry registry;
    private final Set<String> metricNameList = new LinkedHashSet<>();

    public MetricCollector(MeterRegistry registry) {
        this.registry = registry;
    }

    public void addCategoryCountMetric(final String categoryMetricName) {
        if (!metricNameList.contains(categoryMetricName)) {
            metricNameList.add(categoryMetricName);
            createCounters(categoryMetricName);
        }
    }

    private void createCounters(final String categoryMetricName) {
        pageViewCounter(categoryMetricName);
        sessionFirstCounter(categoryMetricName);
        sessionHitCounter(categoryMetricName);
        sessionAllCounter(categoryMetricName);
    }
    public void clearCounts(final String categoryMetricName) {
        clearCounter(pageViewCounter(categoryMetricName));
        clearCounter(sessionFirstCounter(categoryMetricName));
        clearCounter(sessionHitCounter(categoryMetricName));
        clearCounter(sessionAllCounter(categoryMetricName));
    }
    public void incrementPageView(final String categoryMetricName) {
        pageViewCounter(categoryMetricName).increment();
    }
    public void incrementFirstHit(final String categoryMetricName) {
        sessionFirstCounter(categoryMetricName).increment();
        sessionAllCounter(categoryMetricName).increment();
    }
    public void incrementSecondaryHit(final String categoryMetricName) {
        sessionHitCounter(categoryMetricName).increment();
        sessionAllCounter(categoryMetricName).increment();
    }
    // Counter handles, will make if not there
    public Counter sessionFirstCounter(final String categoryMetricName) {
        return registry.counter(categoryMetricName, VIEWS_TAG, SESSION_FIRST_VAL);
    }
    public Counter sessionHitCounter(final String categoryMetricName) {
        return registry.counter(categoryMetricName, VIEWS_TAG, SESSION_HIT_VAL);
    }
    public Counter sessionAllCounter(final String categoryMetricName) {
        return registry.counter(categoryMetricName, VIEWS_TAG, SESSION_ALL_VAL);
    }
    public Counter pageViewCounter(final String categoryMetricName) {
        return registry.counter(categoryMetricName, VIEWS_TAG, PAGE_VIEW_ALL);
    }

    private void clearCounter(final Counter counter) {
        registry.remove(counter.getId());
    }

}
