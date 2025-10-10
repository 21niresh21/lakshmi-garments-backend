package com.lakshmigarments.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // ✅ Grouped count by Category and SubCategory names
    @Query("SELECT i.category.name, i.subCategory.name, SUM(i.count) " +
           "FROM Inventory i " +
           "GROUP BY i.category.name, i.subCategory.name")
    List<Object[]> getCategorySubCategoryCount();

    // ✅ Lookup by subCategory name and category name (both are directly available from Inventory)
    @Query("SELECT i FROM Inventory i WHERE i.subCategory.name = :subCategoryName AND i.category.name = :categoryName")
    Optional<Inventory> findBySubCategoryNameAndCategoryName(@Param("subCategoryName") String subCategoryName,
                                                             @Param("categoryName") String categoryName);

    // ✅ Dynamic filter by category name and/or subCategory name
    @Query("SELECT i FROM Inventory i " +
           "WHERE (:category IS NULL OR i.category.name = :category) " +
           "AND (:subCategory IS NULL OR i.subCategory.name = :subCategory)")
    Page<Inventory> findByCategoryAndSubCategory(@Param("category") String category,
                                                 @Param("subCategory") String subCategory,
                                                 Pageable pageable);

    // ✅ Lookup by category ID and subCategory ID
    @Query("SELECT i FROM Inventory i WHERE i.category.id = :categoryId AND i.subCategory.id = :subCategoryId")
    Optional<Inventory> findByCategoryIdAndSubCategoryId(@Param("categoryId") Long categoryId,
                                                         @Param("subCategoryId") Long subCategoryId);
}
