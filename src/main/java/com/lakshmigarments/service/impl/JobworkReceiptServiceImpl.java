package com.lakshmigarments.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.DamageDTO;
import com.lakshmigarments.dto.JobworkReceiptDTO;
import com.lakshmigarments.dto.JobworkReceiptItemDTO;
import com.lakshmigarments.exception.ItemNotFoundException;
import com.lakshmigarments.exception.JobworkItemNotFoundException;
import com.lakshmigarments.exception.JobworkNotFoundException;
import com.lakshmigarments.exception.UserNotFoundException;
import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.BatchStatus;
import com.lakshmigarments.model.Damage;
import com.lakshmigarments.model.DamageType;
import com.lakshmigarments.model.Item;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.model.JobworkItem;
import com.lakshmigarments.model.JobworkItemStatus;
import com.lakshmigarments.model.JobworkReceipt;
import com.lakshmigarments.model.JobworkReceiptItem;
import com.lakshmigarments.model.JobworkStatus;
import com.lakshmigarments.model.User;
import com.lakshmigarments.repository.*;
import com.lakshmigarments.service.JobworkReceiptService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobworkReceiptServiceImpl implements JobworkReceiptService {

    private final DamageRepository damageRepository;

	private final JobworkReceiptRepository jobworkReceiptRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(JobworkReceiptServiceImpl.class);
	private final JobworkRepository jobworkRepository;
	private final BatchRepository batchRepository;
	private final UserRepository userRepository;
	private final JobworkReceiptItemRepository receiptItemRepository;
	private final ItemRepository itemRepository;
	private final JobworkItemRepository jobworkItemRepository;

	@Override
	@Transactional
	public void createJobworkReceipt(JobworkReceiptDTO jobworkReceipt) {

		String jobworkNumber = jobworkReceipt.getJobworkNumber();
		Long receivedById = jobworkReceipt.getReceivedById();
		List<JobworkReceiptItemDTO> jobworkReceiptItemDTOs = jobworkReceipt.getJobworkReceiptItems();

		Jobwork jobwork = jobworkRepository.findByJobworkNumber(jobworkNumber).orElseThrow(() -> {
			LOGGER.error("Jobwork with number {} not found", jobworkNumber);
			return new JobworkNotFoundException("Jobwork with number " + jobworkNumber + " not found");
		});

		User user = userRepository.findById(receivedById).orElseThrow(() -> {
			LOGGER.error("User with ID {} not found", receivedById);
			return new UserNotFoundException("User not found with ID " + receivedById);
		});

		JobworkReceipt newJobworkReceipt = new JobworkReceipt();
		newJobworkReceipt.setCompletedBy(jobwork.getEmployee());
		newJobworkReceipt.setJobwork(jobwork);
		newJobworkReceipt.setReceivedBy(user);
		JobworkReceipt createdJobworkReceipt = jobworkReceiptRepository.save(newJobworkReceipt);

		long itemCount = 0;
		for (JobworkReceiptItemDTO jobworkReceiptItemDTO : jobworkReceiptItemDTOs) {
			String itemName = jobworkReceiptItemDTO.getItemName();
			Item existingItem = itemRepository.findByName(itemName).orElseThrow(() -> {
				LOGGER.error("Item not found with name {}", itemName);
				return new ItemNotFoundException("Item not found with name " + itemName);
			});
			
			itemCount += jobworkReceiptItemDTO.getReturnedQuantity();
			JobworkItem jobworkItem = jobworkItemRepository
					.findByJobworkJobworkNumber(jobworkNumber).orElseThrow(() -> {
						LOGGER.error("JobworkItem with jobwork number {} not found", jobworkNumber);
						return new JobworkItemNotFoundException("JobworkItem not found with jobwork number "
								+ jobworkNumber);
					});
			

			JobworkReceiptItem jobworkReceiptItem = new JobworkReceiptItem();
			jobworkReceiptItem.setItem(existingItem);
			jobworkReceiptItem.setPurchaseQuantity(jobworkReceiptItemDTO.getPurchasedQuantity());
			jobworkReceiptItem.setPurchaseRate(jobworkReceiptItemDTO.getPurchaseCost());
			jobworkReceiptItem.setJobworkReceipt(createdJobworkReceipt);
			jobworkReceiptItem.setReceivedQuantity(jobworkReceiptItemDTO.getReturnedQuantity());
			jobworkReceiptItem.setWagePerItem(jobworkReceiptItemDTO.getWage());

			long totalDamagedQuantity = 0;
			for (DamageDTO damageDTO : jobworkReceiptItemDTO.getDamages()) {
				Damage damage = new Damage();
				damage.setQuantity(damageDTO.getQuantity());
				damage.setDamageType(DamageType.fromString(damageDTO.getType()));
				damage.setJobworkItem(jobworkItem);
				damageRepository.save(damage);
				
				if (DamageType.fromString(damageDTO.getType()) == DamageType.REPAIRABLE) {
					itemCount += damageDTO.getQuantity() == null ? 0 : damageDTO.getQuantity();
				}
				
				totalDamagedQuantity += damageDTO.getQuantity();
			}
			
			long totalQuantityForItem = jobworkReceiptItemDTO.getPurchasedQuantity() + 
					jobworkReceiptItemDTO.getReturnedQuantity() + totalDamagedQuantity ;
			if (jobworkItem.getQuantity() == totalQuantityForItem) {
				Batch batch = jobwork.getBatch();
				batch.setAvailableQuantity(itemCount);
				jobworkItem.setJobworkStatus(JobworkItemStatus.COMPLETED);
				batch.setBatchStatus(BatchStatus.COMPLETED);
				jobwork.setJobworkStatus(JobworkStatus.COMPLETED);
				batchRepository.save(batch);
				jobworkRepository.save(jobwork);
			}
			
			jobworkReceiptItem.setDamagedQuantity(totalDamagedQuantity);
			receiptItemRepository.save(jobworkReceiptItem);

		}

		// increase quantity for batch
		
		
		// TODO to make the batch CLOSED if all jobworks quantity are done for this batch

	}


}
