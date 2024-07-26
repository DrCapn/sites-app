package com.example.sites.domain;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.example.sites.domain.SitesService.*;

@Value
public class InstanceCategory {

    private static final Logger log = LoggerFactory.getLogger(InstanceCategory.class);

    public final static String LANDING_LABEL="landing";

    String label;
    String lookupName;
    @NonFinal
    String[] subCategories;
    @NonFinal
    boolean parent;
    boolean landing;

    public InstanceCategory(final String lookupName) {
        subCategories = null;
        parent = false;
        if (lookupName.startsWith(DEFAULT_PAGE)) {
            landing = true;
            this.lookupName = lookupName + TEMPLATE_SUFFIX;
            label = LANDING_LABEL;
        } else {
            landing = false;
            this.lookupName = lookupName;
            label = lookupName;
        }
    }

    public InstanceCategory(final String lookupName, final String label) {
        subCategories = null;
        parent = false;
        this.lookupName = lookupName;
        // do not override label value for index when set in constructor
        this.label = label;
        landing = label.equals(LANDING_LABEL);
    }

    public InstanceCategory(final String lookupName, final String label, final String[] subCategories) {
        this.subCategories = subCategories;
        parent = (subCategories != null && subCategories.length > 0);
        this.lookupName = lookupName;
        // do not override label value for index when set in constructor
        this.label = label;
        landing = label.equals(LANDING_LABEL);
    }
    public void update(final String subCat) {
        if (subCat.startsWith(DEFAULT_PAGE)) {
            parent = true;
        } else {
            List<String> subCatList;
            if (subCategories != null && subCategories.length > 0) {
                subCatList = new ArrayList<>(Arrays.asList(subCategories));
            } else {
                subCatList = new ArrayList<>();
            }
            subCatList.add(subCat);
            subCategories = subCatList.toArray(new String[0]);
        }
    }
}
