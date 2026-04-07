package com.lakshmigarments.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lakshmigarments.model.BatchItem;
import com.lakshmigarments.model.Item;

public interface BatchItemRepository extends JpaRepository<BatchItem, Long> {

    List<BatchItem> findByBatchId(Long batchId);
    
    Optional<BatchItem> findByBatchIdAndItem(Long batchId, Item item);
    
    Optional<BatchItem> findByBatchIdAndItemName(Long batchId, String itemName);
    
    Optional<BatchItem> findByBatchSerialCodeAndItemName(String serialCode, String itemName);

    List<BatchItem> findByBatchSerialCode(String batchSerial);
}
