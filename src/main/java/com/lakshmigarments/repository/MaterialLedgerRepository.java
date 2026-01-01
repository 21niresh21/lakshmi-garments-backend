package com.lakshmigarments.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lakshmigarments.model.Inventory;
import com.lakshmigarments.model.MaterialInventoryLedger;

public interface MaterialLedgerRepository extends JpaRepository<MaterialInventoryLedger, Long> {

	@Query("SELECT i.category, i.subCategory.name, " +
		       "SUM(CASE WHEN i.direction = 'IN' THEN i.quantity ELSE -i.quantity END), " +
		       "(SUM(CASE WHEN i.direction = 'IN' THEN i.quantity ELSE -i.quantity END) * 100.0 / " +
		       "(SELECT SUM(CASE WHEN i2.direction = 'IN' THEN i2.quantity ELSE -i2.quantity END) " +
		       " FROM MaterialInventoryLedger i2 WHERE i2.category = i.category)) " +
		       "FROM MaterialInventoryLedger i " +
		       "GROUP BY i.category, i.subCategory.name")
		List<Object[]> getCategorySubCategoryCountWithPercentage();
		
	@Query("SELECT i FROM MaterialInventoryLedger i WHERE i.category.id = :categoryId AND i.subCategory.id = :subCategoryId")
	Optional<MaterialInventoryLedger> findByCategoryIdAndSubCategoryId(@Param("categoryId") Long categoryId,
	                                                     @Param("subCategoryId") Long subCategoryId);
	
	boolean existsByCategoryIdAndSubCategoryId(Long categoryId, Long subCategoryId);
	
	@Query("""
		    SELECT COALESCE(SUM(
		        CASE WHEN i.direction = 'IN' THEN i.quantity ELSE -i.quantity END
		    ), 0)
		    FROM MaterialInventoryLedger i
		    WHERE i.category.id = :categoryId
		      AND i.subCategory.id = :subCategoryId
		""")
		Long getAvailableQuantityByCategoryAndSubCategory(
		        @Param("categoryId") Long categoryId,
		        @Param("subCategoryId") Long subCategoryId
		);

	
}
