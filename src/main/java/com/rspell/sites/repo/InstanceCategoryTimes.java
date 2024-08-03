package com.rspell.sites.repo;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

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
