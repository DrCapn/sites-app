package com.rspell.sites.domain;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;

import java.util.List;

@Value
@RequiredArgsConstructor
@ToString
public class SitesConfig {

    List<SiteDefinition> sites;
    String version;
}
