package com.lakshmigarments.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.lakshmigarments.model.Supplier;

/**
 * Specification class for creating dynamic queries for Supplier entities.
 * Provides reusable query predicates for common filtering operations.
 */
public class SupplierSpecification {
	
	/**
	 * Creates a specification to filter suppliers by name (case-insensitive partial match).
	 * Returns null if the name parameter is null or blank to avoid applying unnecessary filters.
	 *
	 * @param name The name to search for (case-insensitive partial match)
	 * @return Specification for filtering suppliers, or null if name is blank
	 */
	public static Specification<Supplier> filterByName(String name) {
		
		if (name == null || name.isBlank()) {
            return null;
        }
		
		return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
				"%" + name + "%");
	}

}
