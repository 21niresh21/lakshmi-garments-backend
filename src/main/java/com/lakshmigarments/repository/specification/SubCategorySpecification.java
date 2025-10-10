package com.lakshmigarments.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.lakshmigarments.model.SubCategory;

public class SubCategorySpecification {

    public static Specification<SubCategory> filterByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name + "%");
    }

}
