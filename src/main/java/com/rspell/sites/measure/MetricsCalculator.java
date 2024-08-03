package com.rspell.sites.measure;

import com.rspell.sites.domain.CategoryService;
import com.rspell.sites.domain.SiteInstance;
import com.rspell.sites.repo.CategoryTimesRepository;
import com.rspell.sites.repo.InstanceCategoryTimes;
import com.rspell.sites.repo.SessionInfo;
import com.rspell.sites.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class MetricsCalculator {

    private static final Logger log = LoggerFactory.getLogger(MetricsCalculator.class);

    private final MetricCollector metricCollector;
    private final SessionService sessionService;
    private final CategoryTimesRepository categoryTimesRepository;
    private final CategoryService categoryService;

    public MetricsCalculator(final MetricCollector metricCollector,
                             final SessionService sessionService,
                             final CategoryTimesRepository categoryTimesRepository,
                             final CategoryService categoryService) {
        this.metricCollector = metricCollector;
        this.sessionService = sessionService;
        this.categoryTimesRepository = categoryTimesRepository;
        this.categoryService = categoryService;
    }
    public MetricsCalculations buildInstanceMetrics(final String siteName,
                                                                 final String instName) {

        final Iterable<SessionInfo> sessions = sessionService.findAllInstanceSessions(siteName, instName);
        return buildMetrics(sessions, siteName, instName).get(mash(siteName, instName));
    }

    public Map<String, MetricsCalculations> buildMetrics() {
        final Iterable<SessionInfo> sessions = sessionService.findAllSessions();
        return buildMetrics(sessions, null, null);
    }

    private Map<String, MetricsCalculations> buildMetrics(final Iterable<SessionInfo> sessions,
                             final String siteName,
                             final String instName) {
        Map<String, MetricsCalculations> result = new LinkedHashMap<>();
        gatherSessionMetrics(sessions, result);
        gatherCategoryMetrics(siteName, instName, result);
        return result;
    }

    public void gatherSessionMetrics(final Iterable<SessionInfo> sessions,
                                     final Map<String, MetricsCalculations> result) {
        // Check all sessions in our repo. Each can look at multiple
        // inst pages. Those are recorded in the catJsonmap string
        for (SessionInfo sessionInfo : sessions) {
            Set<String> categoriesClicked = Utils.jsonToSet(sessionInfo.getCategoriesJsonSet());
            MetricsCalculations instanceResult =
                    findCreateMetrics(result, sessionInfo.getSiteName(), sessionInfo.getInstanceName());
            instanceResult.increaseTotalSessions();

            // If a session didnt look at all categories count it
            int expectedTotal = instanceResult.getCategoryCount();
            int clicked = categoriesClicked.size();

            if (clicked == 0) {
                // no cats
                instanceResult.getPercentMetric(Metric.SESS_SEEN_NONE).increaseCount();
            } else if (clicked >= expectedTotal) {
                // viewed all
                instanceResult.getPercentMetric(Metric.SESS_SEEN_ALL).increaseCount();
            } else {
                // some but not all
                instanceResult.getPercentMetric(Metric.SESS_UNSEEN).increaseCount();
            }
        }
    }

    public void gatherCategoryMetrics(final String siteName,
                                      final String instName,
                                      final Map<String, MetricsCalculations> result) {
        // For all instances
        for (SiteInstance siteInstance : categoryService.getRegisteredInstances(siteName)) {
            String thisSiteName = siteInstance.getData().getSiteName();
            String thisInstName = siteInstance.getName();

            // if not matching and looking for a match, skip
            if ((siteName != null && !thisSiteName.equals(siteName)) &&
                    (instName != null && !thisInstName.equals(instName))) {
                continue;
            }

            MetricsCalculations metricsCalculations = findCreateMetrics(result, thisSiteName, thisInstName);
            metricsCalculations.addClickMetricType(Metric.CLICK_SESS_ALL);
            metricsCalculations.addClickMetricType(Metric.CLICK_SESS_FIRST);
            metricsCalculations.addClickMetricType(Metric.CLICK_SESS_HIT);
            metricsCalculations.addClickMetricType(Metric.CLICK_SESS_VIEWS);
            metricsCalculations.addTimeMetricType(Metric.TIME_VIEWED);

            for (String catName : categoryService.getCategoryNames(thisSiteName, thisInstName)) {
                // For all categories - get click metrics
                String name = CategoryService.createCounterName(thisSiteName, thisInstName, catName, null);
                CategoryClickMetric clickMetric = CategoryClickMetric.builder()
                        .categoryName(catName).build();
                clickMetric.addMetricCount(Metric.CLICK_SESS_ALL, metricCollector.sessionAllCounter(name).count());
                clickMetric.addMetricCount(Metric.CLICK_SESS_FIRST, metricCollector.sessionFirstCounter(name).count());
                clickMetric.addMetricCount(Metric.CLICK_SESS_HIT, metricCollector.sessionHitCounter(name).count());
                clickMetric.addMetricCount(Metric.CLICK_SESS_VIEWS, metricCollector.pageViewCounter(name).count());
                metricsCalculations.addClickMetric(catName, clickMetric);

                // For all categories - get tiem metrics
                final String instanceCategory = CategoryService.createInstanceCategoryName(thisSiteName, thisInstName, catName);
                InstanceCategoryTimes times = categoryTimesRepository.findTimesByInstanceCategory(instanceCategory);
                CategoryTimeMetric timeMetric = CategoryTimeMetric.builder()
                        .categoryName(catName)
                        .numTimes(times != null ? times.getNumTimes() : 0L)
                        .totalTimes(times != null ? times.getTotalTimes() : 0L)
                        .build();

                if (times != null) {
                    log.info("{} todo log times", instanceCategory);
                } else {
                    log.info("{} null times", instanceCategory);
                }
                metricsCalculations.addTimeMetric(catName, timeMetric);
            }
        }
    }

    // Collect cat metrics from aggregator
    public MetricsCalculations findCreateMetrics(final Map<String, MetricsCalculations> metricsPerInstance,
                                                 final String siteName,
                                                 final String instName) {
        String key = mash(siteName, instName);
        MetricsCalculations instMetrics = metricsPerInstance.get(key);

        // encountered instance for the first time in our result list
        // Note: the order here impacts the order displayed on reports
        if (instMetrics == null) {
            instMetrics = new MetricsCalculations();
            instMetrics.setCategoryCount(categoryService.getCategoryCount(siteName, instName));
            instMetrics.addPercentMetric(Metric.SESS_SEEN_ALL,
                    CategoryPercentMetric.builder()
                            .metric(Metric.SESS_SEEN_ALL)
                            .zeroText("No sessions clicked all categories.")// TODO
                            .allText("All sess") // TODO
                            .count(0L)
                            .build());
            instMetrics.addPercentMetric(Metric.SESS_SEEN_NONE,
                    CategoryPercentMetric.builder()
                            .metric(Metric.SESS_SEEN_NONE)
                            .zeroText("All clicked at least 1")// TODO
                            .allText("All didnt") // TODO
                            .count(0L)
                            .build());
            instMetrics.addPercentMetric(Metric.SESS_UNSEEN,
                    CategoryPercentMetric.builder()
                            .metric(Metric.SESS_UNSEEN)
                            .zeroText("None click some.")// TODO
                            .allText("all click some") // TODO
                            .count(0L)
                            .build());
            metricsPerInstance.put(key, instMetrics);
        }
        return instMetrics;
    }

    private String mash(final String site, final String instance) {
        return site + "/" + instance;
    }
}
