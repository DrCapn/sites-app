package com.example.sites.config;

import com.example.sites.SessionRecorderInterceptor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.validation.constraints.NotNull;

@Configuration
@ComponentScan("com.example.sites")
public class MvcConfig implements WebMvcConfigurer {

    // TODO ? not sure this is needed
//    private final SitesService sitesService;
//    public MvcConfig(final SitesService sitesService) {
//        super();
//        this.sitesService = sitesService;
//    }

    @Override
    public void addViewControllers(@NotNull ViewControllerRegistry registry) {
        registry.addViewController("/health")
                .setViewName("forward:/health.html");
        registry.addViewController("/")
                .setViewName("forward:/index.html");
    }

    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new SessionRecorderInterceptor());
    }
}
