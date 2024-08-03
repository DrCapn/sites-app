package com.rspell.sites.measure;

import com.rspell.sites.domain.CategoryService;
import com.rspell.sites.domain.SiteInstance;
import com.rspell.sites.domain.SitesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/siteMetrics")
public class MetricsController {

    private static final Logger log = LoggerFactory.getLogger(MetricsController.class);

    private final SitesService sitesService;
    private final CategoryService categoryService;
    private final SessionService sessionService;
    private final MetricsCalculator metricsCalculator;

    public MetricsController(SitesService sitesService,
                             CategoryService categoryService,
                             SessionService sessionService,
                             MetricsCalculator metricsCalculator) {
        this.sitesService = sitesService;
        this.categoryService = categoryService;
        this.sessionService = sessionService;
        this.metricsCalculator = metricsCalculator;
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<String> clearAllSessions() {
        long count = sessionService.removeAllSessions();
        if (count < 0) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/sessions/{siteName}")
    public ResponseEntity<String> clearSiteSessions(@PathVariable final String siteName) {

        if (sitesService.isInvalidSite(siteName)) {
            return new ResponseEntity<>(siteName, HttpStatus.NOT_FOUND);
        }
        long count = sessionService.removeAllSiteSessions(siteName);
        categoryService.clearSiteCounts(siteName);
        return new ResponseEntity<>(String.valueOf(count), HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/sessions/{siteName}/{instName}")
    public ResponseEntity<String> clearInstanceSessions(@PathVariable final String siteName,
                                                        @PathVariable final String instName) {

        if (categoryService.isInvalidInstance(siteName, instName)) {
            return new ResponseEntity<>(instName, HttpStatus.NOT_FOUND);
        }
        long count = sessionService.removeAllInstanceSessions(siteName, instName);
        categoryService.clearInstanceCounts(siteName, instName);
        return new ResponseEntity<>(String.valueOf(count), HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/sessions/{siteName}/{instName}/current")
    public ResponseEntity<String> clearCurrentInstanceSession(@PathVariable final String siteName,
                                                              @PathVariable final String instName,
                                                              HttpSession session) {

        if (categoryService.isInvalidInstance(siteName, instName)) {
            return new ResponseEntity<>(instName, HttpStatus.NOT_FOUND);
        }
        sessionService.removeSessionInstance(session.getId(), siteName, instName);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/instances")
    public String getRunningInstances(final Model model) {
        setDefaultTemplateValues(model);
        List<SiteInstance> instanceList = sitesService.getAllSites();
        model.addAttribute("instanceList", instanceList);
        return "reports/instances";
    }

    @GetMapping("/reports/all")
    public String getAllInstancesReport(final Model model) {
        setDefaultTemplateValues(model);
        Map<String, MetricsCalculations> sessionMetricsPerInstance = metricsCalculator.buildMetrics();

        model.addAttribute("instanceList", sessionMetricsPerInstance.keySet());
        model.addAttribute("metricsPerInstance", sessionMetricsPerInstance);
        return "reports/allInstanceMetrics";
    }

    @GetMapping("/reports/{siteName}/{instName}")
    public String getInstanceReport(@PathVariable final String siteName,
                                    @PathVariable final String instName,
                                    final Model model) {
        setDefaultTemplateValues(model);
        MetricsCalculations metricsCalculations =
                metricsCalculator.buildInstanceMetrics(siteName, instName);
        model.addAttribute("siteName", siteName);
        model.addAttribute("instanceName", instName);
        model.addAttribute("metrics", metricsCalculations);
        return "reports/instanceMetrics";
    }

    private static final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.FULL, Locale.US);
    private void setDefaultTemplateValues(final Model model) {
        model.addAttribute("creationTime", df.format(new Date()));
    }
}
