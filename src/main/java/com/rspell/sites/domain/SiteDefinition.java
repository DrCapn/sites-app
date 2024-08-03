package com.rspell.sites.domain;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;

import java.util.Set;

@Value
@RequiredArgsConstructor
@ToString
public class SiteDefinition {

    String siteName;
    Set<String> siteInstances;
    Set<String> metrics;
    Boolean allowClear;
    Boolean persistMetrics;
    String version;
}
