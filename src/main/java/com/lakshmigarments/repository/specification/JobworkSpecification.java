package com.lakshmigarments.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.lakshmigarments.model.Jobwork;

public class JobworkSpecification {

    public static Specification<Jobwork> filterUniqueByJobworkNumber(String jobworkNumber) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true); // ensures distinct results
            String pattern = "%" + jobworkNumber + "%";
            return criteriaBuilder.like(root.get("jobworkNumber"), pattern);
        };
    }

}
