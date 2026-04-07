package com.lakshmigarments.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

	@Query("SELECT i.category.name, SUM(CASE WHEN i.direction = 'IN' THEN i.quantity ELSE -i.quantity END) " +
           "FROM MaterialInventoryLedger i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY i.category.name")
    List<Object[]> findCategoryDistributionBetweenDates(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    @Query("SELECT i.subCategory.name, SUM(CASE WHEN i.direction = 'IN' THEN i.quantity ELSE -i.quantity END), i.category.name " +
           "FROM MaterialInventoryLedger i " +
           "WHERE i.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY i.subCategory.name, i.category.name")
    List<Object[]> findSubCategoryDistributionBetweenDates(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    @Query("SELECT FUNCTION('DATE', i.createdAt), SUM(i.quantity) " +
           "FROM MaterialInventoryLedger i " +
           "WHERE i.direction = 'IN' AND i.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE', i.createdAt)")
    List<Object[]> findQuantityTrendBetweenDates(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);


    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM MaterialInventoryLedger i " +
           "WHERE i.direction = 'IN' AND i.createdAt BETWEEN :startDate AND :endDate")
    Long findTotalQuantityProcessedBetweenDates(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(i.quantity * (SELECT COALESCE(AVG(b.price), 0) FROM Bale b WHERE b.subCategory = i.subCategory)), 0) " +
           "FROM MaterialInventoryLedger i " +
           "WHERE i.direction = 'IN' AND i.createdAt BETWEEN :startDate AND :endDate")
    Double calculateWeeklyInventoryValue(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    @Query("""
    	    SELECT COALESCE(SUM(CASE WHEN m.direction = 'IN' THEN m.quantity ELSE -m.quantity END), 0)
    	    FROM MaterialInventoryLedger m
    	    WHERE m.category.id = :categoryId AND m.subCategory.id = :subCategoryId
    	""")
    	Long getStockCount(@Param("categoryId") Long categoryId, @Param("subCategoryId") Long subCategoryId);
    
 // For getCategorySubCategoryCounts()
    @Query("""
    	    SELECT 
    	        m.category.name, 
    	        m.category.code, 
    	        m.subCategory.name,
    	        SUM(CASE WHEN m.direction = 'IN' THEN m.quantity ELSE -m.quantity END)
    	    FROM MaterialInventoryLedger m
    	    GROUP BY m.category.id, m.category.name, m.category.code, 
    	             m.subCategory.id, m.subCategory.name
    	""")
    	List<Object[]> getStockGroupedByCategory();
}


