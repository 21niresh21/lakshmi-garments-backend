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
import com.lakshmigarments.model.DamageSource;
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
			// this means its not the first jobwork which is item based
            // so we need to dynamically calculate the available quantities based on the target jobwork type
            
            List<BatchItemResponse> batchItemResponses = new ArrayList<>();
            long i = 1;
            
            for (BatchItem batchItem : batchItems) {
                String itemName = batchItem.getItem().getName();
                Long totalBatchItemQuantity = batchItem.getQuantity();
                
                Long availableQuantity = calculateAvailableQuantityForJobworkType(
                    serialCode, targetJobworkType, itemName, totalBatchItemQuantity);
                LOGGER.debug("avaialble quantity {}", availableQuantity);
                
                if (availableQuantity != null && availableQuantity > 0) {
                    BatchItemResponse batchItemResponse = new BatchItemResponse();
                    batchItemResponse.setName(itemName);
                    batchItemResponse.setAvailableQuantity(availableQuantity);
                    batchItemResponse.setId(i);
                    batchItemResponses.add(batchItemResponse);
                    i += 1;
                }
            }
            
            return batchItemResponses;
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
//        return calculateAvailableQuantitiesForStage(serialCode, targetJobworkType, batchItems);
    }
    
    /**
     * Calculates available quantity for a specific jobwork type based on workflow dependencies.
     * 
     * @param serialCode Batch serial code
     * @param targetJobworkType The jobwork type we're calculating availability for
     * @param itemName The item name
     * @param totalBatchItemQuantity Total quantity from batch items (for fallback)
     * @return Available quantity for the target jobwork type
     */
    private Long calculateAvailableQuantityForJobworkType(
            String serialCode, 
            JobworkType targetJobworkType,
            String itemName,
            Long totalBatchItemQuantity) {
        
        switch (targetJobworkType) {
            case EMBROIDERY:
                // EMBROIDERY receives from CUTTING
                // Available = Cutting Received - Embroidery Accepted (already processed) 
                //             - Embroidery In-Progress + Repairable Embroidery Damages (from PREVIOUS_JOBWORK only)
                Long cuttingReceived = receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                    serialCode, JobworkType.CUTTING.name(), itemName);
                
                Long embroideryAccepted = receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                    serialCode, JobworkType.EMBROIDERY.name(), itemName);
                
                Long embroideryInProgress = jobworkRepository.getAssignedQuantities(
                    serialCode, JobworkType.EMBROIDERY.name(), itemName);
                
                // Only count repairable damages from PREVIOUS_JOBWORK (not CURRENT_JOBWORK which is already assigned)
                Long repairableEmbroideryDamages = damageRepository.getDamagedQuantityByDamageSource(serialCode, 
                    DamageType.REPAIRABLE.name(), JobworkType.EMBROIDERY.name(), itemName, DamageSource.PREVIOUS_JOBWORK.name());
                
                // Subtract embroidery accepted because those pieces have already moved past embroidery
                long availableForEmbroidery = cuttingReceived - embroideryAccepted - embroideryInProgress + repairableEmbroideryDamages;
                LOGGER.debug("EMBROIDERY Item {}: Cutting received {} - Embroidery accepted {} - Embroidery in-progress {} + Repairable damages {} = Available {}", 
                    itemName, cuttingReceived, embroideryAccepted, embroideryInProgress, repairableEmbroideryDamages, availableForEmbroidery);
                return availableForEmbroidery;
                
            case STITCHING:
                // STITCHING receives from EMBROIDERY (if done) or CUTTING (if embroidery skipped)
                // Available = Input Quantity (Embroidery or Cutting) - Stitching In-Progress - embroidery in-progress + Repairable Stitching Damages
                
                Long embroideryReceived = receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                    serialCode, JobworkType.EMBROIDERY.name(), itemName);
                
                Long assignedEmbroidery = jobworkRepository.getAssignedQuantitiesInProgress(serialCode, 
                		JobworkType.EMBROIDERY.name(), itemName);
                LOGGER.debug(" EMBR {}",assignedEmbroidery);
                
                // Use embroidery output if available, otherwise use cutting output
                Long inputQuantityForStitching = (embroideryReceived != null && embroideryReceived > 0) 
                    ? embroideryReceived 
                    : receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                        serialCode, JobworkType.CUTTING.name(), itemName);
                
                Long stitchingInProgress = jobworkRepository.getAssignedQuantities(
                    serialCode, JobworkType.STITCHING.name(), itemName);
                
                // Only count repairable damages from PREVIOUS_JOBWORK
                Long repairableStitchingDamages = damageRepository.getDamagedQuantityByDamageSource(serialCode, 
                    DamageType.REPAIRABLE.name(), JobworkType.STITCHING.name(), itemName, DamageSource.PREVIOUS_JOBWORK.name());
                
                long availableForStitching = inputQuantityForStitching - stitchingInProgress + repairableStitchingDamages - assignedEmbroidery;
                LOGGER.debug("STITCHING Item {}: Input {} - Stitching in-progress {} + Repairable damages {} - Assigned in progress embroidery {} = Available {}", 
                    itemName, inputQuantityForStitching, stitchingInProgress, repairableStitchingDamages, assignedEmbroidery, availableForStitching);
                return availableForStitching;
                
            case OVERLOCK:
                // OVERLOCK receives from STITCHING
                // Available = Stitching Received - Overlock In-Progress + Repairable Overlock Damages
                
                Long stitchingReceivedForOverlock = receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                    serialCode, JobworkType.STITCHING.name(), itemName);
                
                Long overlockInProgress = jobworkRepository.getAssignedQuantities(
                    serialCode, JobworkType.OVERLOCK.name(), itemName);
                
                Long repairableOverlockDamages = damageRepository.getDamagedQuantityByDamageSource(serialCode, 
                    DamageType.REPAIRABLE.name(), JobworkType.OVERLOCK.name(), itemName, DamageSource.PREVIOUS_JOBWORK.name());
                
                long availableForOverlock = stitchingReceivedForOverlock - overlockInProgress + repairableOverlockDamages;
                LOGGER.debug("OVERLOCK Item {}: Stitching received {} - Overlock in-progress {} + Repairable damages {} = Available {}", 
                    itemName, stitchingReceivedForOverlock, overlockInProgress, repairableOverlockDamages, availableForOverlock);
                return availableForOverlock;
                
            case IRONING:
                // IRONING receives from OVERLOCK (if done) or STITCHING (if overlock skipped)
                // Available = Input Quantity (Overlock or Stitching) - Ironing In-Progress + Repairable Ironing Damages
                
                Long overlockReceived = receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                    serialCode, JobworkType.OVERLOCK.name(), itemName);
                
                // Use overlock output if available, otherwise use stitching output
                Long inputQuantityForIroning = (overlockReceived != null && overlockReceived > 0) 
                    ? overlockReceived 
                    : receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                        serialCode, JobworkType.STITCHING.name(), itemName);
                
                Long ironingInProgress = jobworkRepository.getAssignedQuantities(
                    serialCode, JobworkType.IRONING.name(), itemName);
                
                Long repairableIroningDamages = damageRepository.getDamagedQuantityByDamageSource(serialCode, 
                    DamageType.REPAIRABLE.name(), JobworkType.IRONING.name(), itemName, DamageSource.PREVIOUS_JOBWORK.name());
                
                long availableForIroning = inputQuantityForIroning - ironingInProgress + repairableIroningDamages;
                LOGGER.debug("IRONING Item {}: Input {} - Ironing in-progress {} + Repairable damages {} = Available {}", 
                    itemName, inputQuantityForIroning, ironingInProgress, repairableIroningDamages, availableForIroning);
                return availableForIroning;
                
            case PACKAGING:
                // PACKAGING receives from STITCHING
                // Available = Stitching Received - Packaging In-Progress + Repairable Packaging Damages
                
                Long stitchingReceivedForPackaging = receiptItemRepository.getAcceptedQuantityByBatchAndJobworkTypeAndItem(
                    serialCode, JobworkType.STITCHING.name(), itemName);
                
                Long packagingInProgress = jobworkRepository.getAssignedQuantities(
                    serialCode, JobworkType.PACKAGING.name(), itemName);
                
                Long repairablePackagingDamages = damageRepository.getDamagedQuantityByDamageSource(serialCode, 
                    DamageType.REPAIRABLE.name(), JobworkType.PACKAGING.name(), itemName, DamageSource.PREVIOUS_JOBWORK.name());
                
                long availableForPackaging = stitchingReceivedForPackaging - packagingInProgress + repairablePackagingDamages;
                LOGGER.debug("PACKAGING Item {}: Stitching received {} - Packaging in-progress {} + Repairable damages {} = Available {}", 
                    itemName, stitchingReceivedForPackaging, packagingInProgress, repairablePackagingDamages, availableForPackaging);
                return availableForPackaging;
                
            default:
                // For CUTTING or unknown types, return batch item quantity
                return totalBatchItemQuantity;
        }
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
