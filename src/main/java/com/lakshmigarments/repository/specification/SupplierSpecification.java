package com.lakshmigarments.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.lakshmigarments.model.Supplier;

public class SupplierSpecification {
	
public static Specification<Supplier> filterByName(String name) {
		
		if (name == null || name.isBlank()) {
            return null;
        }
		
		return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
				"%" + name + "%");
	}

}
