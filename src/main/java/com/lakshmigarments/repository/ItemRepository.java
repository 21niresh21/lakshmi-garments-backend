package com.lakshmigarments.repository;

import com.lakshmigarments.model.Item;
import jakarta.validation.Valid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item,Long>, JpaSpecificationExecutor<Item> {
    boolean existsByNameIgnoreCase(String name);

    @Modifying
    @Query("DELETE FROM Item i WHERE i.id = :id")
    long deleteItemById(Long id);
}
