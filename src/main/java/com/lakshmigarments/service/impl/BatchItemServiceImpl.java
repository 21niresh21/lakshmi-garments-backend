package com.lakshmigarments.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.response.BatchItemResponse;
import com.lakshmigarments.exception.BatchItemNotFoundException;
import com.lakshmigarments.model.BatchItem;
import com.lakshmigarments.model.DamageType;
import com.lakshmigarments.model.JobworkType;
import com.lakshmigarments.service.BatchItemService;
import com.lakshmigarments.repository.BatchItemRepository;
import com.lakshmigarments.repository.DamageRepository;
import com.lakshmigarments.repository.JobworkReceiptRepository;
import com.lakshmigarments.repository.JobworkRepository;
import com.lakshmigarments.repository.JobworkReceiptItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchItemServiceImpl implements BatchItemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchItemServiceImpl.class);
    private final BatchItemRepository batchItemRepository;
    private final JobworkReceiptRepository receiptRepository;
    private final JobworkRepository jobworkRepository;
    private final DamageRepository damageRepository;
    private final JobworkReceiptItemRepository receiptItemRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<BatchItemResponse> getBatchItemsByBatchSerial(String serialCode, String jobworkType) {
        LOGGER.debug("Fetching items by batch serial: {} for jobwork type: {}", serialCode, jobworkType);
        
        JobworkType targetJobworkType = JobworkType.fromString(jobworkType)
            .orElseThrow(() -> new IllegalArgumentException("Invalid jobwork type: " + jobworkType));
        
        // Get all batch items for reference
        List<BatchItem> batchItems = batchItemRepository.findByBatchSerialCode(serialCode);
        
        boolean isNotFirstItemJobForBatch = jobworkRepository.existsByBatchSerialCodeForItem(serialCode);
        LOGGER.debug("Item based jobwork has already been issues {} ", isNotFirstItemJobForBatch);
        if (isNotFirstItemJobForBatch) {
			
		} else {
			List<BatchItemResponse> batchItemResponses = new ArrayList<>();
	        long i = 1;
	        for (BatchItem batchItem : batchItems) {
				Long totalBatchItemQuantity = batchItem.getQuantity();
				Long totalAssignedQuantity = jobworkRepository.getAssignedQuantities(serialCode, 
						jobworkType, batchItem.getItem().getName());
				Long totalRepairableDamagesForItem = damageRepository.getDamagedQuantity(serialCode, 
						DamageType.REPAIRABLE.name(), jobworkType, batchItem.getItem().getName());
				LOGGER.debug("Batch item {} {} quantities : original quantity {}, repairable damages {}, assigned quantity {}", 
						serialCode, batchItem.getItem().getName(), totalBatchItemQuantity, totalRepairableDamagesForItem, totalAssignedQuantity);
				
				Long availableQuantity = totalBatchItemQuantity - totalAssignedQuantity + totalRepairableDamagesForItem;
				BatchItemResponse batchItemResponse = new BatchItemResponse();
				batchItemResponse.setName(batchItem.getItem().getName());
				batchItemResponse.setAvailableQuantity(availableQuantity);
				batchItemResponse.setId(i);
				
				batchItemResponses.add(batchItemResponse);
				i += 1;
				
			}
	        return batchItemResponses;
		}
        
        // Calculate available quantities dynamically based on workflow stage
        return calculateAvailableQuantitiesForStage(serialCode, targetJobworkType, batchItems);
    }
    
    /**
     * Dynamically calculates available quantities for any jobwork stage.
     * 
     * Formula: Available = (Input from Previous Stage Receipts) 
     *                      - (Assigned to In-Progress Jobworks) 
     *                      + (Repairable Damages)
     */
    private List<BatchItemResponse> calculateAvailableQuantitiesForStage(
            String serialCode, 
            JobworkType targetJobworkType,
            List<BatchItem> batchItems) {
        
        List<BatchItemResponse> responses = new ArrayList<>();
        long i = 1;
        
        for (BatchItem batchItem : batchItems) {
            String itemName = batchItem.getItem().getName();
            
            // Get input quantity from previous stage's completed receipts
            Long inputFromPrevious = getInputFromPreviousStageReceipts(serialCode, targetJobworkType, itemName);
            
            // Skip CUTTING as it uses sub-category quantities (handled separately)
            if (targetJobworkType == JobworkType.CUTTING && inputFromPrevious == 0) {
                continue;
            }
            
            // Get quantities currently assigned to open jobworks (IN_PROGRESS, PENDING_RETURN, etc.)
            Long assignedToOpenJobworks = jobworkRepository.getAssignedQuantities(
                serialCode, targetJobworkType.name(), itemName);
            
            // Get repairable damages that can be reassigned
            Long repairableDamages = damageRepository.getDamagedQuantity(
                serialCode, DamageType.REPAIRABLE.name(), 
                targetJobworkType.name(), itemName);
            
            // Calculate final available quantity
            Long availableQuantity = inputFromPrevious - assignedToOpenJobworks + repairableDamages;
            
            LOGGER.debug("Item {} | Input: {}, Assigned: {}, Repairable: {}, Available: {}",
                itemName, inputFromPrevious, assignedToOpenJobworks, repairableDamages, availableQuantity);
            
            if (availableQuantity != null && availableQuantity > 0) {
                BatchItemResponse response = new BatchItemResponse();
                response.setName(itemName);
                response.setAvailableQuantity(availableQuantity);
                response.setId(i);
                responses.add(response);
                i++;
            }
        }
        
        LOGGER.debug("Total items for {} in batch {}: {}", targetJobworkType, serialCode, responses.size());
        return responses;
    }
    
    /**
     * Determines the input quantity source based on the target jobwork type.
     * Handles workflow dependencies dynamically.
     */
    private Long getInputFromPreviousStageReceipts(String serialCode, JobworkType targetType, String itemName) {
        return switch (targetType) {
            case CUTTING -> {
                // CUTTING uses sub-category quantities, not batch items
                // Return 0 to skip this in the main loop
                yield 0L;
            }
            case EMBROIDERY -> {
                // Embroidery receives output from Cutting
                yield receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                    serialCode, JobworkType.CUTTING.name(), itemName);
            }
            case STITCHING -> {
                // Stitching can receive from either Embroidery (if done) or Cutting (if embroidery skipped)
                Long fromEmbroidery = receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                    serialCode, JobworkType.EMBROIDERY.name(), itemName);
                
                // Use embroidery output if available, otherwise use cutting output
                yield (fromEmbroidery != null && fromEmbroidery > 0) ? fromEmbroidery :
                    receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                        serialCode, JobworkType.CUTTING.name(), itemName);
            }
            case PACKAGING -> {
                // Packaging receives output from Stitching
                yield receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                    serialCode, JobworkType.STITCHING.name(), itemName);
            }
            case OVERLOCK -> {
                // Overlock receives output from Cutting
                yield receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                    serialCode, JobworkType.CUTTING.name(), itemName);
            }
            case IRONING -> {
                // Ironing receives output from Stitching (or Packaging if done)
                Long fromPackaging = receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                    serialCode, JobworkType.PACKAGING.name(), itemName);
                yield (fromPackaging != null && fromPackaging > 0) ? fromPackaging :
                    receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                        serialCode, JobworkType.STITCHING.name(), itemName);
            }
        };
    }
    
    private BatchItem getBatchItemOrThrow(String serialCode, String itemName) {
		return batchItemRepository.findByBatchSerialCodeAndItemName(serialCode, itemName).orElseThrow(() -> {
			LOGGER.error("Batch item not found: {} {}", serialCode, itemName);
			return new BatchItemNotFoundException("Batch item not found: " + serialCode + " " + itemName);
		});
	}

}
