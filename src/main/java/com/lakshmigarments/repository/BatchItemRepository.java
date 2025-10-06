package com.lakshmigarments.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lakshmigarments.model.BatchItem;

public interface BatchItemRepository extends JpaRepository<BatchItem, Long> {

    List<BatchItem> findByBatchId(Long batchId);

}
