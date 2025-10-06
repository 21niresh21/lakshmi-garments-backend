package com.lakshmigarments.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.BatchSubCategory;
import java.util.List;


public interface BatchSubCategoryRepository extends JpaRepository<BatchSubCategory, Long> {

	List<BatchSubCategory> findByBatch(Batch batch);

	List<BatchSubCategory> findByBatchId(Long batchId);
	
}
