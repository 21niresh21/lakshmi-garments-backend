package com.lakshmigarments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.BatchSubCategory;
import java.util.List;


public interface BatchSubCategoryRepository extends JpaRepository<BatchSubCategory, Long> {

	List<BatchSubCategory> findByBatch(Batch batch);

	List<BatchSubCategory> findByBatchId(Long batchId);
	
	@Query(value = "select sum(bsc.available_quantity) from batch_sub_categories bsc, batches b\r\n"
			+ "where b.id = bsc.batch_id and b.id = ?1;", nativeQuery = true)
	Long findRemainingUnitsInBatch(Long batchId);
	
}
