package com.rspell.sites.config;

import com.rspell.sites.domain.SitesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.UrlTemplateResolver;

@Configuration
public class ThymeLeafConfig {

    private static final Logger log = LoggerFactory.getLogger(ThymeLeafConfig.class);

    private final SitesService sitesService;

    public ThymeLeafConfig(SitesService sitesService) {
        this.sitesService = sitesService;
    }

    @Bean
    public SpringResourceTemplateResolver classpathSitesTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix(sitesService.getClassPathTemplatesLoc());
        setCommonWebSettings(resolver, 1);
        return resolver;
    }

    @Bean
    public FileTemplateResolver fileSitesTemplateResolver() {
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(sitesService.getFileTemplatesLoc());
        setCommonWebSettings(resolver, 2);
        return resolver;
    }

    @Bean
    public UrlTemplateResolver urlSitesTemplateResolver() {
        String prefix = sitesService.getUrlTemplatesLoc();
        if (prefix != null) {
            UrlTemplateResolver resolver = new UrlTemplateResolver();
            resolver.setPrefix(prefix);
            setCommonWebSettings(resolver, 3);
        }
        return null;
    }

    private void setCommonWebSettings(AbstractConfigurableTemplateResolver resolver, int order) {
        resolver.setSuffix(SitesService.TEMPLATE_SUFFIX);
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(order);
        resolver.setCheckExistence(true);
        resolver.setCacheable(false);
    }
}
