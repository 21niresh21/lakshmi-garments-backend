package com.lakshmigarments.repository;

import com.lakshmigarments.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item,Long>, JpaSpecificationExecutor<Item> {
    boolean existsByNameIgnoreCase(String name);
    void deleteById(Long id);
}
