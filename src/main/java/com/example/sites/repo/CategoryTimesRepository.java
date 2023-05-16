package com.example.sites.repo;

import org.springframework.data.repository.CrudRepository;

public interface CategoryTimesRepository extends CrudRepository<InstanceCategoryTimes, String> {

    InstanceCategoryTimes findTimesByInstanceCategory(String instanceCategory);
}
