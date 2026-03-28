package com.lakshmigarments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.BatchSubCategory;
import java.util.List;


public interface BatchSubCategoryRepository extends JpaRepository<BatchSubCategory, Long> {

	List<BatchSubCategory> findByBatch(Batch batch);

	List<BatchSubCategory> findByBatchId(Long batchId);

	@Query("SELECT bsc FROM BatchSubCategory bsc WHERE bsc.batch.serialCode = :serialCode AND bsc.subCategory.name = :subCategoryName")
	java.util.Optional<BatchSubCategory> findByBatchSerialCodeAndSubCategoryName(String serialCode, String subCategoryName);
	
	@Query(value = "select sum(bsc.available_quantity) from batch_sub_categories bsc, batches b\r\n"
			+ "where b.id = bsc.batch_id and b.id = ?1;", nativeQuery = true)
	Long findRemainingUnitsInBatch(Long batchId);
	
	@Query("SELECT bsc.subCategory.name FROM BatchSubCategory bsc WHERE bsc.batch.serialCode = :serialCode")
	List<String> findSubCategoriesBySerialCode(String serialCode);
	
}
