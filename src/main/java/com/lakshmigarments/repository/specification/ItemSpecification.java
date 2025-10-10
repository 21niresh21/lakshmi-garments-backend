package com.lakshmigarments.repository.specification;

import com.lakshmigarments.model.Item;
import org.springframework.data.jpa.domain.Specification;

public class ItemSpecification {
    public static Specification<Item> filterByName(String name){
        if(name == null || name.isBlank()){
            return null;
        }
        return(root,query,criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                "%" + name + "%");
    }
}
