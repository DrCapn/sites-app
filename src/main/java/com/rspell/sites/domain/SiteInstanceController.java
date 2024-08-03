package com.rspell.sites.domain;

import com.rspell.sites.measure.MetricCollector;
import com.rspell.sites.measure.SessionService;
import com.rspell.sites.repo.InstanceCategoryTimes;
import com.rspell.sites.repo.SessionInfo;
import com.rspell.sites.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

@Controller
public class SiteInstanceController {
    private static final Logger log = LoggerFactory.getLogger(SiteInstanceController.class);

    private final MetricCollector metricCollector;
    private final SessionService sessionService;
    private final CategoryService categoryService;
    private final SitesService sitesService;

    public SiteInstanceController(MetricCollector metricCollector, SessionService sessionService, CategoryService categoryService, SitesService sitesService) {
        this.metricCollector = metricCollector;
        this.sessionService = sessionService;
        this.categoryService = categoryService;
        this.sitesService = sitesService;
    }

    @GetMapping(path = "/{siteName}/{instName:^(?!.*scripts).+}/{catName}")
    public String postToCategoryPage(@PathVariable final String siteName,
                                     @PathVariable final String instName,
                                     @PathVariable final String catName,
                                     @RequestBody Map<String, String> keyValuePairs,
                                     final HttpSession session,
                                     final Model model) {

        SiteInstance siteInstance = getSite(siteName, instName);
        InstanceCategory instanceCategory = getCategory(siteInstance, catName);
        // todo do we handle metrics?
        handleMetrics(siteInstance, instanceCategory, null, session, model);
        setDataOnModel(model, keyValuePairs);
        return sitesService.formTemplateLookup(siteInstance, instanceCategory);
    }

    @PostMapping(path = "/{siteName}/{instName:^(?!.*scripts).+}")
    public String getInstancePage(@PathVariable final String siteName,
                                  @PathVariable final String instName,
                                  @RequestParam(required = false) final String data,
                                  final HttpSession session,
                                  final Model model) {

        SiteInstance siteInstance = getSite(siteName, instName);
        InstanceCategory landing = categoryService.getLanding(siteInstance);
        if (landing == null) {
            handleNotFound("todo site inst not found");
        }
        handleMetrics(siteInstance, landing, null, session, model);
        setDataOnModel(model, data);
        return sitesService.formTemplateLookup(siteInstance, landing);
    }

    @GetMapping(path = "/{siteName}/{instName:^(?!.*style).+}/{catName}")
    public String getCategoryPage(@PathVariable final String siteName,
                                  @PathVariable final String instName,
                                  @PathVariable final String catName,
                                  @RequestParam(required = false) final String data,
                                  final HttpSession session,
                                  final Model model) {

        SiteInstance siteInstance = getSite(siteName, instName);
        InstanceCategory instanceCategory = getCategory(siteInstance, catName);
        handleMetrics(siteInstance, instanceCategory, null, session, model);
        setDataOnModel(model, data);
        return sitesService.formTemplateLookup(siteInstance, instanceCategory);
    }

    @GetMapping(path = "/{siteName}/{instName:^(?!.*style).+}/{catName}/{subCatName}")
    public String getCategorySubPage(@PathVariable final String siteName,
                                     @PathVariable final String instName,
                                     @PathVariable final String catName,
                                     @PathVariable final String subCatName,
                                     @RequestParam(required = false) final String data,
                                     final HttpSession session,
                                     final Model model) {

        SiteInstance siteInstance = getSite(siteName, instName);
        InstanceCategory instanceCategory = getCategory(siteInstance, catName);
        validateSubCat(siteInstance, instanceCategory, subCatName);
        handleMetrics(siteInstance, instanceCategory, subCatName, session, model);
        setDataOnModel(model, data);
        return sitesService.formTemplateLookup(siteInstance, instanceCategory, subCatName);
    }
    private void handleMetrics(SiteInstance siteInstance,
                               InstanceCategory instanceCategory,
                               String subCatName,
                               HttpSession session,
                               Model model) {

        String siteName = siteInstance.getData().getSiteName();
        String instName = siteInstance.getName();
        String categoryName = instanceCategory.getLookupName();
        if (subCatName != null) {
            categoryName = CategoryService.combine(categoryName, subCatName);
        }
        // bypass logic, session attr setting
        boolean doMetrics = true; // was bypass stuff
//        model.addAttribute("metricStatus")
        // log
        if (doMetrics) {
            // register
            sessionService.findRegisterSession(session, siteName, instName);
            final String categoryMetricName = CategoryService.createCounterName(siteInstance, instanceCategory, subCatName);
            if (instanceCategory.isLanding()) {
                metricCollector.incrementPageView(categoryMetricName);
            } else {
                recordViews(session, siteName, instName, categoryName, categoryMetricName);
            }
            handleCategoryTime(session, siteInstance, instanceCategory.getLabel());
        }
    }
    private void recordViews(HttpSession session, String site, String inst, String cat, String catMetricName) {

        SessionInfo sessionInfo = sessionService.findRegisterSession(session, site, inst);
        String sessionId = sessionInfo.getSessionInstanceId();
        Set<String> sessionCategories = Utils.jsonToSet(sessionInfo.getCategoriesJsonSet());

        if (sessionCategories.isEmpty()) {
            sessionCategories.add(cat);
            metricCollector.incrementFirstHit(catMetricName);
            // log
            sessionService.updateSession(sessionInfo, sessionCategories);
        } else if (!sessionCategories.contains(cat)) {
            sessionCategories.add(cat);
            metricCollector.incrementSecondaryHit(catMetricName);
            // log
            sessionService.updateSession(sessionInfo, sessionCategories);
        }
        metricCollector.incrementPageView(catMetricName);
        // log
    }
    private void handleCategoryTime(HttpSession session, SiteInstance siteInstance, String catTimeName) {
        String lastCatTimeName = (String) session.getAttribute("lastCategory");
        long currTimeMillis = System.currentTimeMillis();
        log.info("todo");
        if (lastCatTimeName != null && !catTimeName.equals(lastCatTimeName)) {
            InstanceCategoryTimes catTimes =
                    categoryService.findCreateCategoryTimes(
                            siteInstance.getData().getSiteName(), siteInstance.getName(), lastCatTimeName);
            long timeOnLastCat = 0L;
            Long lastCategoryTime = (Long) session.getAttribute("lastCategoryTime");
            if(lastCategoryTime != null) {
                timeOnLastCat = currTimeMillis -lastCategoryTime;
            }
            categoryService.incrementTimes(catTimes, timeOnLastCat);
        }
        session.setAttribute("lastCategory", catTimeName);
        session.setAttribute("lastCategoryTime", currTimeMillis);
    }

    private SiteInstance getSite(String site, String inst) {
        SiteInstance siteInstance = sitesService.getSiteInstance(site, inst);
        if (siteInstance == null) {
            handleNotFound("site not found todo");
        }
        return siteInstance;
    }

    private InstanceCategory getCategory(SiteInstance siteInstance, String categoryName) {
        InstanceCategory instanceCategory =
                categoryService.getCategory(siteInstance, categoryName);
        if (instanceCategory == null) {
            handleNotFound("cat not found todo");
        }
        return instanceCategory;
    }
    private void validateSubCat(SiteInstance siteInstance, InstanceCategory instanceCategory, String subCat) {
        if (instanceCategory.isParent() &&
                !Arrays.asList(instanceCategory.getSubCategories()).contains(subCat)) {
            handleNotFound("not found todo");
        }
    }
//    private void handleNotFound(String site, String inst, String cat) {
//    }
//    private void handleNotFound(String site, String inst, String cat, String subCat) {
//    }
    private void handleNotFound(final String err) {
        log.error(err);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, err);
    }

    public ModelAndView handleError(HttpServletRequest req, Exception ex) {
        log.error("Request: {} raised {} ", req.getRequestURL(), ex, ex);

        ModelAndView mav = new ModelAndView();
        if (ex.getMessage().startsWith("Error on loading Sites")) {
            mav.addObject("exception", ex.getMessage());
        } else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            mav.addObject("exception", sw.toString());
        }
        mav.addObject("url", req.getRequestURL());
        mav.setViewName("error");
        return mav;
    }
    private void setDataOnModel(final Model model, final Map<String, String> data) {
        for (String key : data.keySet()) {
            log.info("key={} value={}", key, data.get(key));
        }
        model.addAllAttributes(data);
    }
    private void setDataOnModel(final Model model, final String data) {
        if (data == null || data.length() == 0) return;

        String[] dataPairs = data.split(";");
        for(String dataPair : dataPairs) {
            String[] keyValue = dataPair.split(":");
            if (keyValue.length != 2) {
                log.warn("too long");
                continue;
            }
            // log key value todo
            model.addAttribute(keyValue[0], keyValue[1]);
        }
    }
}
