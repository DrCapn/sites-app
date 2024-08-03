package com.rspell.sites.repo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class InstanceCategoryTimes {

    // Id formed from <instanceName>-<categoryName>
    @Id
    private String instanceCategory;
    private Long totalTimes;
    private Long numTimes;
}
