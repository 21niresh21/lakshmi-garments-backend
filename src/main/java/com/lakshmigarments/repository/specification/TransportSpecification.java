package com.lakshmigarments.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.lakshmigarments.model.Transport;

public class TransportSpecification {

    public static Specification<Transport> filterByName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                "%" + name + "%");
    }

}
