package com.rspell.sites.domain;

import com.rspell.sites.measure.MetricCollector;
import com.rspell.sites.repo.CategoryTimesRepository;
import com.rspell.sites.repo.InstanceCategoryTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    // site.instance.counter.category
    private static final String COUNTER_TEMPLATE = "%s.%s.counter.%s";
    // metricName.subCategory
    private static final String SUBCAT_TEMPLATE = "%s.%s";

    private final CategoryTimesRepository categoryTimesRepository;
    private final MetricCollector metricCollector;
    private final SitesService sitesService;

    public CategoryService(final CategoryTimesRepository categoryTimesRepository,
                           final MetricCollector metricCollector,
                           final SitesService sitesService) {
        this.categoryTimesRepository = categoryTimesRepository;
        this.metricCollector = metricCollector;
        this.sitesService = sitesService;
    }

    public static String createCounterName(final SiteInstance instance,
                                           final InstanceCategory category) {
        return createCounterName(instance.getData().getSiteName(),
                instance.getName(), category.getLabel(), null);
    }

    public static String createCounterName(final SiteInstance instance,
                                           final InstanceCategory category,
                                           final String subCatName) {
        return createCounterName(instance.getData().getSiteName(),
                instance.getName(), category.getLabel(), subCatName);
    }

    public static String createCounterName(final String siteName,
                                           final String instName,
                                           final String catName,
                                           final String subCatName) {
        String metricName = String.format(COUNTER_TEMPLATE, siteName, instName, catName);
        metricName = metricName.replace('-', '.');
        if (subCatName != null) {
            metricName = String.format(SUBCAT_TEMPLATE, metricName, subCatName);
        }
        return metricName;
    }

    public static String combine(final String category, final String subCategory) {
        return category + "-" + subCategory;
    }

    public boolean isInvalidInstance(final String siteName, final String instanceName) {
        return sitesService.isInvalidInstance(siteName, instanceName);
    }

    public InstanceCategory getLanding(final SiteInstance site) {
        return site.getCategories()
                .stream()
                .filter(InstanceCategory::isLanding)
                .findFirst()
                .orElse(null);
    }

    public InstanceCategory getCategory(final SiteInstance site, final String categoryName) {
        return sitesService.getCategory(site, categoryName);
    }

    public int getCategoryCount(final String siteName, final String instanceName) {
        return sitesService.getSiteInstance(siteName, instanceName).getCategoryCount();
    }

    public Set<String> getCategoryNames(final String siteName, final String instanceName) {
        return sitesService.getSiteInstance(siteName, instanceName).getCategoryNames();
    }

    public List<SiteInstance> getRegisteredInstances(final String siteName) {
        if (siteName == null) {
            return sitesService.getAllSites();
        }
        return sitesService.getSiteInstances(siteName);
    }

    // catTimeName will be either the category name or category-with-subCat name
    public InstanceCategoryTimes findCreateCategoryTimes(final String siteName,
                                                         final String instanceName,
                                                         final String catTimeName) {
        final String instCatName = createInstanceCategoryName(siteName, instanceName, catTimeName);
        InstanceCategoryTimes times = categoryTimesRepository.findTimesByInstanceCategory(instCatName);
        if (times == null) {
            times = new InstanceCategoryTimes();
            times.setInstanceCategory(instCatName);
            times.setTotalTimes(0L);
            times.setNumTimes(0L);
            categoryTimesRepository.save(times);
        }
        return times;
    }
    public void incrementTimes(InstanceCategoryTimes times, long time) {
        // log
        times.setTotalTimes(times.getTotalTimes() + time);
        times.setNumTimes(times.getNumTimes() + 1L);
        categoryTimesRepository.save(times);
        // log
    }

    // TODO REWRITE WITH STREAMS?
    public void clearAllCounts() {
        categoryTimesRepository.deleteAll();
        for(SiteInstance siteInstance : getRegisteredInstances(null)) {
            for (InstanceCategory instanceCategory : siteInstance.getCategories()) {
                metricCollector.clearCounts(
                        createCounterName(siteInstance, instanceCategory, null));
                if (instanceCategory.isParent()) {
                    for (String subCatName : instanceCategory.getSubCategories()) {
                        metricCollector.clearCounts(
                                createCounterName(siteInstance, instanceCategory, subCatName));
                    }
                }
            }
        }
    }

    public void clearSiteCounts(final String siteName) {
        for(SiteInstance siteInstance : getRegisteredInstances(siteName)) {
            for (InstanceCategory instanceCategory : siteInstance.getCategories()) {
                clearCategoryCounts(siteInstance, instanceCategory);
            }
        }
    }

    public void clearInstanceCounts(final String siteName, final String instanceName) {
        final SiteInstance siteInstance = sitesService.getSiteInstance(siteName, instanceName);
        if (siteInstance != null) {
            for (InstanceCategory instanceCategory : siteInstance.getCategories()) {
                clearCategoryCounts(siteInstance, instanceCategory);
            }
        }
    }

    public void clearCategoryCounts(final SiteInstance siteInstance, final InstanceCategory instanceCategory) {
        String siteName = siteInstance.getData().getSiteName();
        String instName = siteInstance.getName();
        String catName = instanceCategory.getLabel();
        String thisLookupName = createCounterName(siteName, instName, catName, null);
        metricCollector.clearCounts(thisLookupName);
        categoryTimesRepository.deleteById(createInstanceCategoryName(siteName, instName, catName));
        if (instanceCategory.isParent()) {
            for (String subCatName : instanceCategory.getSubCategories()) {
                thisLookupName = createCounterName(siteName, instName, catName, subCatName);
                metricCollector.clearCounts(thisLookupName);

                thisLookupName = CategoryService.combine(catName, subCatName);
                categoryTimesRepository.deleteById(createInstanceCategoryName(siteName, instName, thisLookupName));
            }
        }
    }

    public static String createInstanceCategoryName(String siteName, String instanceName, String catName) {
        return siteName + "-" + instanceName + "-" + catName;
    }
}
