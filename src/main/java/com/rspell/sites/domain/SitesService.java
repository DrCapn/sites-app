package com.rspell.sites.domain;

import com.rspell.sites.config.SitesAppConfig;
import com.rspell.sites.measure.MetricCollector;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Component
public class SitesService {

    private static final Logger log = LoggerFactory.getLogger(SitesService.class);
    public static final String DEFAULT_PAGE = "index";
    public static final String TEMPLATE_SUFFIX = ".html";

    public static final String FILE_PREFIX = "file:";
    public static final String CLASSPATH_PREFIX = "classpath:";
    public static final String URL_PREFIX = "http";
    public static final String DIR_TEMPLATE = "%s/%s/";
    public static final String LOOKUP_TEMPLATE = "%s/%s/%s";
    public static final String DEFAULT_TEMPLATE_LOC = "sites/templates";

    private final SitesAppConfig appConfig;
    private final ObjectMapper objectMapper;
    private final ApplicationContext appContext;
    private final MetricCollector metricCollector;
    private Map<String, List<SiteInstance>> siteToInstances;

    // TODO remove or make better
    private final static StringBuffer ERRORS = new StringBuffer();

    public SitesService(final SitesAppConfig appConfig,
                        final ObjectMapper objectMapper,
                        final ApplicationContext appContext,
                        final MetricCollector metricCollector) {
        this.appConfig = appConfig;
        this.objectMapper = objectMapper;
        this.appContext = appContext;
        this.metricCollector = metricCollector;

        try {
            siteToInstances = initSiteToInstances(siteToInstances);
            loadSites(siteToInstances);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            ERRORS.append(":: ").append(sw);
            log.error("Cannot load sites config", e);
        }
    }

    public String getClassPathTemplatesLoc() {
        if(appConfig.getSitesLocation().startsWith(CLASSPATH_PREFIX)) {
            String prefix = siteTemplates();
            log.info("Site templates classpath location: {}", prefix);
            return prefix;
        }
        return String.format(DIR_TEMPLATE, CLASSPATH_PREFIX, DEFAULT_TEMPLATE_LOC);
    }

    public String getFileTemplatesLoc() {
        if(appConfig.getSitesLocation().startsWith(FILE_PREFIX)) {
            String prefix = siteTemplates().substring(FILE_PREFIX.length());
            log.info("Site templates file location: {}", prefix);
            return prefix;
        }
        return DEFAULT_TEMPLATE_LOC;
    }

    public String getUrlTemplatesLoc() {
        if(appConfig.getSitesLocation().startsWith(URL_PREFIX)) {
            String prefix = siteTemplates();
            log.info("Site templates url location: {}", prefix);
            return prefix;
        }
        return null;
    }

    public SiteInstance getSiteInstance(String siteName, String instanceName) {
        if (siteToInstances == null) {
            throw new RuntimeException("Error on loadingSites - " + ERRORS.toString());
        }
        List<SiteInstance> instList = siteToInstances.get(siteName);
        if (instList == null) {
            return null;
        }
        return instList.stream()
                .filter(si-> si.getName().equals(instanceName))
                .findFirst()
                .orElse(null);
    }

    public List<SiteInstance> getAllSites() {
        LinkedList<SiteInstance> sites = new LinkedList<>();
        siteToInstances.values().forEach(sites::addAll);
        return sites;
    }

    public List<SiteInstance> getSiteInstances(String siteName) {
        return new LinkedList<>(siteToInstances.get(siteName));
    }

    public boolean isInvalidSite(String siteName) {
        return !siteToInstances.containsKey(siteName);
    }
    public boolean isInvalidInstance(String siteName, String instanceName) {
        if (!siteToInstances.containsKey(siteName)) {
            return false;
        }
        return (getSiteInstance(siteName, instanceName) == null);
    }

    public InstanceCategory getCategory(final SiteInstance site, final String categoryName) {
        return site.getCategories()
                .stream()
                .filter(ic -> ic.getLookupName().equals(categoryName))
                .findFirst()
                .orElse(null);
    }

    public String formTemplateLookup(final SiteInstance siteInstance,
                                     final InstanceCategory instanceCategory) {
        String lookup = String.format(LOOKUP_TEMPLATE,
                siteInstance.getData().getSiteName(),
                siteInstance.getName(),
                instanceCategory.getLookupName());
        if (instanceCategory.isParent()) {
            lookup = lookup + "/" + DEFAULT_PAGE + TEMPLATE_SUFFIX;
        }
        return lookup;
    }

    public String formTemplateLookup(final SiteInstance siteInstance,
                                     final InstanceCategory instanceCategory,
                                     final String subCatName) {
        return String.format(LOOKUP_TEMPLATE,
                siteInstance.getData().getSiteName(),
                siteInstance.getName(),
                instanceCategory.getLookupName())
                + "/" + subCatName;
    }

    private String siteTemplates() {
        return String.format(DIR_TEMPLATE, appConfig.getSitesLocation(), appConfig.getSitesTemplates());
    }

    private void loadSites(Map<String, List<SiteInstance>> newSiteToInstances) throws IOException {
        final String siteLoc = appConfig.getSitesLocation();
        // File sitesFile = appContext.getResource(siteLoc + "/sitesConfig.json").getFile();
        InputStream sitesFile =
                appContext.getResource(siteLoc + "/sitesConfig.json").getInputStream();

        SitesConfig sitesConfig =
                objectMapper.readValue(sitesFile, SitesConfig.class);
        log.info(sitesConfig.toString());
        Map<String, List<SiteInstance>> sitesToInstances =
                new LinkedHashMap<>(sitesConfig.getSites().size());

        // for each site, read template files and create category names
        for(SiteDefinition site : sitesConfig.getSites()) {
            String siteNameTemplateDir = siteTemplates() + site.getSiteName();
            List<SiteInstance> siteInstances = new LinkedList<>();
            newSiteToInstances.put(site.getSiteName(), siteInstances);

            for (String instanceName : site.getSiteInstances()) {
                SiteInstance siteInstance = new SiteInstance(
                        site, instanceName, new LinkedHashSet<>(), 0, new LinkedHashSet<>());
                siteInstances.add(siteInstance);

                String instNameDir = String.format(DIR_TEMPLATE, siteNameTemplateDir, instanceName);
                log.info(instNameDir);
                processSiteResources(instanceName, siteInstance, instNameDir);
            }
        }
    }

    private Map<String, List<SiteInstance>> initSiteToInstances(
            final Map<String, List<SiteInstance>> oldSiteToInstances) {
        // Clear out the old mapping and return a new one
        log.info("Clearing the in memory sites");
        // TODO - how this gets cleared in java may impact performance
        //      do we need to save/persist the old set? debug option (not avail while perf test)
        //      Possibly create perf test that rigorously sets new site instances
        //      (lots there, lots to clear, also what if adding and clearing? race? is that possible?

        // TODO - double check the clear() process here
        // TODO - ? grab the old size, but is it worth it.
        Optional.ofNullable(oldSiteToInstances).ifPresent(Map::clear);
        return new HashMap<>(10);
    }

    private void processSiteResources(String instanceName, SiteInstance siteInstance, String instNameDir) throws IOException {
        ERRORS.append(instNameDir);
        log.info(instNameDir);
        Resource[] resources;
        try {
            resources = appContext.getResources(instNameDir + "**/*" + TEMPLATE_SUFFIX);
        } catch (IOException ioe) {
            ERRORS.append(instanceName).append(" had an IOException: ").append(ioe);
            log.warn("{} IOException", instanceName, ioe);
            return;
        }

        if (resources.length == 0) {
            ERRORS.append(instanceName).append(" has noresources. \n");
            log.warn("{} DOESNT HAVE RESOURCES", instanceName);
            return;
        }

        log.info("Num of resources: {}", resources.length);
        for (Resource resource : resources) {
            log.info("    -- new resource --");
            ERRORS.append(resource.getFilename())
                    .append(": exists-").append(resource.exists())
                    .append(": isFile-").append(resource.isFile()).append("\n");
            log.info("{}: exists-{}: isFile-{}\n", resource.getFilename(), resource.exists(), resource.isFile());
            log.info(">URI-{}", resource.getURI());

            String resName = resource.getFilename();
            String resInstName = resource.getURI().toString();
            resInstName = resInstName.split(siteInstance.getName() + "/")[1];
            String[] subDirSplit = resInstName.split("/");
            if (subDirSplit.length == 1) {
                // process file as category
                recordCategory(getCategoryName(resName), siteInstance);

            } else if (subDirSplit.length == 2) {
                // process as subdir
                String catName = subDirSplit[0];
                log.info("{} SUBCAT {} item: {}", instanceName, catName, resName);
                recordCategory(catName, catName, getCategoryName(resName), siteInstance);

            } else {
                log.warn("too many subdirs to support right now");
            }
        }
        updateInstance(siteInstance);
    }

    private void updateInstance(SiteInstance siteInstance) {
        for (InstanceCategory cat : siteInstance.getCategories()) {
            metricCollector.addCategoryCountMetric(
                    CategoryService.createCounterName(siteInstance, cat));
            if (cat.isParent() && cat.getSubCategories() != null) {
                for (String subCat : cat.getSubCategories()) {
                    metricCollector.addCategoryCountMetric(
                            CategoryService.createCounterName(siteInstance, cat, subCat));
                }
            }
        }
    }
    private void recordCategory(String catLookupName, String catLabel, String subCat, SiteInstance siteInst) {
        boolean isNew = false;

        InstanceCategory instanceCategory = getCategory(siteInst, catLookupName);
        if (instanceCategory == null) {
            isNew = true;
            instanceCategory = new InstanceCategory(catLookupName, catLabel, new String[0]);
        }
        instanceCategory.update(subCat);
        if (isNew) {
            siteInst.getCategories().add(instanceCategory);
            siteInst.getCategoryNames().add(instanceCategory.getLabel());
            siteInst.incCategoryCount(1);
        }
        if (!subCat.startsWith("index")) {
            String catName = instanceCategory.getLabel();
            siteInst.getCategoryNames().add(CategoryService.combine(catName, subCat));
            siteInst.incCategoryCount(1);
        }
        log.info("CATEGORY: {}, ISPARENT: {}. SUB: {}, NUM-SUBS: {}, FOR {}/{}",
                catLookupName, instanceCategory.isParent(), subCat,
                (instanceCategory.getSubCategories() == null) ? "null" : instanceCategory.getSubCategories().length,
                siteInst.getData().getSiteName(), siteInst.getName());
    }

//    private void recordCategory(String catLookupName, String label, List<String> subCats, SiteInstance siteInstance) {
//    }

    private void recordCategory(String catLookupName, SiteInstance siteInst) {
        // TODO validate file name and/or enforce rules? (spaces with actuator/prometheus?)
        InstanceCategory instanceCategory = new InstanceCategory(catLookupName);
        siteInst.getCategories().add(instanceCategory);
        siteInst.getCategoryNames().add(instanceCategory.getLabel());
        if (!instanceCategory.isLanding()) {
            siteInst.incCategoryCount(1);
        }
        log.info("CATEGORY: {} FOR {}/{}",
                catLookupName, siteInst.getData().getSiteName(), siteInst.getName());
    }

    private String getCategoryName(String resName) {
        return resName.substring(0, resName.length() - TEMPLATE_SUFFIX.length());
    }
}
