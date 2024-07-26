package com.example.sites.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Builder
@Getter
@ToString
public class SiteInstance {

    SiteDefinition data;
    String name;
    Set<InstanceCategory> categories;
    int categoryCount;
    Set<String> categoryNames;

    public void incCategoryCount(int amount) {
        categoryCount += amount;
    }
}
