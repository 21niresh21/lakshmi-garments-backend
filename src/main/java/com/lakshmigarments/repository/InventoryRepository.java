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

	@Query("SELECT i.subCategory.category, i.subCategory.name, SUM(i.count) FROM Inventory i GROUP BY i.subCategory.category, i.subCategory.name")
	List<Object[]> getCategorySubCategoryCount();

	Optional<Inventory> findBySubCategoryNameAndCategoryName(String subCategoryName, String categoryName);

	@Query("SELECT i FROM Inventory i WHERE (:category IS NULL OR i.subCategory.category = :category) AND (:subCategory IS NULL OR i.subCategory.name = :subCategory)")
	Page<Inventory> findByCategoryAndSubCategory(@Param("category") String category,
			@Param("subCategory") String subCategory, Pageable pageable);

	@Query("SELECT i FROM Inventory i WHERE i.subCategory.category.id = :categoryId AND i.subCategory.id = :subCategoryId")
	Optional<Inventory> findByCategoryIdAndSubCategoryId(@Param("categoryId") Long categoryId,
			@Param("subCategoryId") Long subCategoryId);

}