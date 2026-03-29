package com.lakshmigarments.service.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.cors.CorsConfigurationSource;

import com.lakshmigarments.dto.request.CreateJobworkReceiptRequest;
import com.lakshmigarments.configuration.SecurityConfig;
import com.lakshmigarments.controller.BatchController;
import com.lakshmigarments.controller.StockController;
import com.lakshmigarments.dto.request.CreateDamageRequest;
import com.lakshmigarments.dto.request.CreateJobworkReceiptItemRequest;
import com.lakshmigarments.exception.BatchNotFoundException;
import com.lakshmigarments.exception.InvalidJobworkItemException;
import com.lakshmigarments.exception.ItemNotFoundException;
import com.lakshmigarments.exception.JobworkClosedException;
import com.lakshmigarments.exception.JobworkItemNotFoundException;
import com.lakshmigarments.exception.JobworkNotFoundException;
import com.lakshmigarments.exception.UserNotFoundException;
import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.BatchItem;
import com.lakshmigarments.model.BatchStatus;
import com.lakshmigarments.model.Damage;
import com.lakshmigarments.model.DamageSource;
import com.lakshmigarments.model.DamageType;
import com.lakshmigarments.model.Item;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.model.JobworkItem;
import com.lakshmigarments.model.JobworkItemStatus;
import com.lakshmigarments.model.JobworkReceipt;
import com.lakshmigarments.model.JobworkReceiptItem;
import com.lakshmigarments.model.JobworkStatus;
import com.lakshmigarments.model.JobworkType;
import com.lakshmigarments.model.User;
import com.lakshmigarments.repository.*;
import com.lakshmigarments.service.BatchService;
import com.lakshmigarments.service.JobworkReceiptService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobworkReceiptServiceImpl implements JobworkReceiptService {

    private final BatchController batchController;

    private final StockController stockController;

    private final SecurityConfig securityConfig;

	private final DamageRepository damageRepository;

	private final JobworkReceiptRepository jobworkReceiptRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(JobworkReceiptServiceImpl.class);
	private final JobworkRepository jobworkRepository;
	private final BatchRepository batchRepository;
	private final UserRepository userRepository;
	private final JobworkReceiptItemRepository receiptItemRepository;
	private final ItemRepository itemRepository;
	private final JobworkItemRepository jobworkItemRepository;
	private final BatchItemRepository batchItemRepository;
	private final BatchService batchService;
	
	@Override
	@Transactional
	public void createJobworkReceipt(CreateJobworkReceiptRequest jobworkReceiptRequest) {
		LOGGER.debug("Creating jobwork receipt");
		
		List<String> itemNames = jobworkItemRepository.findItemNamesByJobworkNumber(jobworkReceiptRequest.getJobworkNumber());
		LOGGER.debug("Items issued for this jobwork {}", itemNames);
		
		Jobwork jobwork = this.getJobworkOrThrow(jobworkReceiptRequest.getJobworkNumber());
		if (jobwork.getJobworkStatus() == JobworkStatus.CLOSED) {
			LOGGER.error("Jobwork already closed.");
			throw new JobworkClosedException("Jobwork already closed");
		}
//		Batch batch = this.getBatchOrThrow(jobwork.getBatch().getSerialCode());
		
		JobworkReceipt jobworkReceipt = new JobworkReceipt();
		jobworkReceipt.setJobwork(jobwork);
		JobworkReceipt createdJobworkReceipt = jobworkReceiptRepository.save(jobworkReceipt);
		LOGGER.debug("Created temporary jobwork receipt");
		
		long totalQuantitiesAccountedForJobwork = 0L;
		for (CreateJobworkReceiptItemRequest receiptItemRequest : jobworkReceiptRequest.getJobworkReceiptItems()) {
			
			if (jobwork.getJobworkType() != JobworkType.CUTTING && !itemNames.contains(receiptItemRequest.getItemName())) {
				LOGGER.error("Item {} not issued for this jobwork: {}", 
						receiptItemRequest.getItemName(), jobworkReceiptRequest.getJobworkNumber());
				throw new InvalidJobworkItemException("Item " + receiptItemRequest.getItemName() 
					+" not issued for this jobwork: " + jobworkReceiptRequest.getJobworkNumber());
			}
			
			LOGGER.debug("Processing the receipt request item {}", receiptItemRequest.getItemName());
			
			Item item = this.getItemOrThrow(receiptItemRequest.getItemName());
			
			JobworkReceiptItem jobworkReceiptItem = new JobworkReceiptItem();
			jobworkReceiptItem.setAcceptedQuantity(receiptItemRequest.getAcceptedQuantity());
			jobworkReceiptItem.setSalesQuantity(receiptItemRequest.getSalesQuantity());
			jobworkReceiptItem.setSalesPrice(receiptItemRequest.getSalesPrice());
			jobworkReceiptItem.setItem(item);
			jobworkReceiptItem.setWagePerItem(receiptItemRequest.getWagePerItem());
			jobworkReceiptItem.setJobworkReceipt(createdJobworkReceipt);
			totalQuantitiesAccountedForJobwork += receiptItemRequest.getAcceptedQuantity() +
					receiptItemRequest.getSalesQuantity();
			
			int totalDamagesForItem = 0;
			if (receiptItemRequest.getDamages() == null || receiptItemRequest.getDamages().isEmpty()) {
				LOGGER.debug("No damages for jobwork receipt item {}", receiptItemRequest.getItemName());
			} else {
				LOGGER.debug("Processing damages for jobwork receipt item {}", receiptItemRequest.getItemName());
				for (CreateDamageRequest damageRequest : receiptItemRequest.getDamages()) {
					boolean isDamagedForDamageType = damageRequest.getQuantity() == 0 ? false : true;
					if (isDamagedForDamageType) {
						Damage damage = new Damage();
						damage.setJobworkReceiptItem(jobworkReceiptItem);
						damage.setQuantity(damageRequest.getQuantity());
						damage.setDamageType(DamageType.valueOf(damageRequest.getType()));
						damage.setReportedFrom(jobwork);
											
						// Set damage source only for REPAIRABLE damage type
						if (damage.getDamageType() == DamageType.REPAIRABLE && damageRequest.getSource() != null && !damageRequest.getSource().isEmpty()) {
							damage.setDamageSource(DamageSource.fromString(damageRequest.getSource()));
												
							// Set causedBy based on damage source
							if (DamageSource.CURRENT_JOBWORK == damage.getDamageSource()) {
								damage.setCausedBy(jobwork);
							} else if (DamageSource.PREVIOUS_JOBWORK == damage.getDamageSource()) {
								// For previous jobwork damage, we need to find the responsible jobwork
								// This can be set later or determined from the batch's jobwork history
								LOGGER.debug("Previous jobwork damage reported for item {}", receiptItemRequest.getItemName());
							}
						}
											
						damageRepository.save(damage);
									
						totalDamagesForItem += damageRequest.getQuantity();
					}
				}
				LOGGER.debug("Total damages for receipt item {}", totalDamagesForItem);
			}
			totalQuantitiesAccountedForJobwork += totalDamagesForItem;
			jobworkReceiptItem.setDamagedQuantity(Long.valueOf(totalDamagesForItem));
			receiptItemRepository.save(jobworkReceiptItem);
			LOGGER.debug("Created jobwork receipt item {}", receiptItemRequest.getItemName());
			
			if (jobwork.getJobworkType() != JobworkType.CUTTING) {
				JobworkItem jobworkItem = jobworkItemRepository
						.findByItemNameAndJobworkJobworkNumber(receiptItemRequest.getItemName(), 
								jobworkReceiptRequest.getJobworkNumber()).orElse(null);

				LOGGER.debug("Computing current status of jobwork item {}", receiptItemRequest.getItemName());
				
				// Calculate total quantities submitted so far for this item across all receipts
				Long totalSubmittedForItem = receiptItemRepository.getTotalSubmittedQuantityForJobworkItem(
						jobwork.getJobworkNumber(), receiptItemRequest.getItemName());
				
				long totalIssuedQuantityForItem = jobworkItem.getQuantity();
				LOGGER.debug("Item {}: issued={}, submitted so far={}", 
						receiptItemRequest.getItemName(), totalIssuedQuantityForItem, totalSubmittedForItem);
				
				if (totalIssuedQuantityForItem == totalSubmittedForItem) {	
					jobworkItem.setJobworkItemStatus(JobworkItemStatus.CLOSED);
					LOGGER.debug("Marked the status of jobwork item {} to CLOSED", receiptItemRequest.getItemName());
				}
				jobworkItemRepository.save(jobworkItem);
			} else {
				// For CUTTING, we mark all input sub-categories as AWAITING_CLOSE when a receipt is submitted
				for (JobworkItem jwi : jobwork.getJobworkItems()) {
					jwi.setJobworkItemStatus(JobworkItemStatus.AWAITING_CLOSE);
					jobworkItemRepository.save(jwi);
				}
				LOGGER.debug("Marked all sub-category jobwork items for 'CUTTING' to AWAITING_CLOSE");
			}
			
			// create batch items or update if CUTTING
			if (jobwork.getJobworkType() == JobworkType.CUTTING) {
				BatchItem batchItem = batchItemRepository.findByBatchIdAndItemName(
						jobwork.getBatch().getId(), receiptItemRequest.getItemName()).orElse(null);
				if (batchItem != null) {
					batchItem.setQuantity(batchItem.getQuantity() + receiptItemRequest.getAcceptedQuantity());
					LOGGER.debug("Updating batch item quantity {} for batch {} item {}", 
							batchItem.getQuantity() + receiptItemRequest.getAcceptedQuantity(), jobwork.getBatch().getSerialCode(), receiptItemRequest.getItemName());
				} else {
					batchItem = new BatchItem();
					batchItem.setBatch(jobwork.getBatch());
					batchItem.setItem(item);
					batchItem.setQuantity(receiptItemRequest.getAcceptedQuantity());
					LOGGER.debug("Created a new batch {} item {}", jobwork.getBatch().getSerialCode(), receiptItemRequest.getItemName());
				}
				batchItemRepository.save(batchItem);
			}
		}
		
		Long totalIssuedQuantityForJobwork = jobworkRepository.findTotalQuantities(jobwork.getJobworkNumber());
		
		// Calculate total quantities submitted across ALL receipts for this jobwork (including current one)
		// Using direct database query to avoid lazy loading issues
		Long totalQuantitiesSubmittedAcrossAllReceipts = receiptItemRepository.getTotalSubmittedQuantityForJobwork(
				jobwork.getJobworkNumber());
		
		LOGGER.debug("Total issued quantity for jobwork {}: {}", jobwork.getJobworkNumber(), totalIssuedQuantityForJobwork);
		LOGGER.debug("Total submitted quantity across all receipts for jobwork {}: {}", 
				jobwork.getJobworkNumber(), totalQuantitiesSubmittedAcrossAllReceipts);
		
		if (totalIssuedQuantityForJobwork.equals(totalQuantitiesSubmittedAcrossAllReceipts) && 
				jobwork.getJobworkType() != JobworkType.CUTTING) {
			jobwork.setJobworkStatus(JobworkStatus.CLOSED);
			LOGGER.info("Marked status of jobwork {} as CLOSED - all quantities accounted for", jobwork.getJobworkNumber());
			
//			Batch batch = this.getBatchOrThrow(jobwork.getBatch().getSerialCode());
//			batch.setBatchStatus(BatchStatus.COMPLETED);
//			batchRepository.save(batch);
//			LOGGER.debug("Marked status of batch {} as COMPLETED", batch.getSerialCode());
			// TODO consider the case of closing the batch if type is packaging and last of its items
			// TODO check if any other jobs are dealing with this batch incomplete
			
		} else {
			jobwork.setJobworkStatus(JobworkStatus.PENDING_RETURN);
			LOGGER.debug("Marked the status of the jobwork {} to PENDING_RETURN");
			if (jobwork.getJobworkType() == JobworkType.CUTTING) {
				jobwork.setJobworkStatus(JobworkStatus.AWAITING_CLOSE);
				LOGGER.debug("Marked the status of the jobwork {} to AWAITING_CLOSE");
			}
			
		}
		Jobwork savedJobwork = jobworkRepository.save(jobwork);
		batchService.recalculateBatchStatus(savedJobwork.getBatch());
	}
	
	
		
	private Jobwork getJobworkOrThrow(String jobworkNumber) {
		return jobworkRepository.findByJobworkNumber(jobworkNumber).orElseThrow(() -> {
			LOGGER.error("Jobwork not found: {}", jobworkNumber);
			return new JobworkNotFoundException("Jobwork not found: " + jobworkNumber);
		});
	}
	
	private Item getItemOrThrow(String itemName) {
		return itemRepository.findByName(itemName).orElseThrow(() -> {
			LOGGER.error("Item not found: {}", itemName);
			return new ItemNotFoundException("Item not found: " + itemName);
		});
	}
	
	private Batch getBatchOrThrow(String serialCode) {
		return batchRepository.findBySerialCode(serialCode).orElseThrow(() -> {
			LOGGER.error("Batch not found: {}", serialCode);
			return new BatchNotFoundException("Batch not found: " + serialCode);
		});
	}

}
