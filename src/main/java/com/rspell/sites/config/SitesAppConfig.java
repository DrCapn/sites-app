package com.rspell.sites.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sitesapp")
public class SitesAppConfig {

    private String sitesLocation = "file:sites";
    private String sitesResources = "resources";
    private String sitesTemplates = "templates";
}
