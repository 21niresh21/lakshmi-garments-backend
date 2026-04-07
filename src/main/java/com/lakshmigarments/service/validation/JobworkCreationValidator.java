package com.lakshmigarments.service.validation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lakshmigarments.dto.request.CreateCuttingJobworkRequest;
import com.lakshmigarments.dto.request.CreateItemBasedJobworkRequest;
import com.lakshmigarments.exception.InsufficientBatchQuantityException;
import com.lakshmigarments.model.DamageType;
import com.lakshmigarments.model.JobworkType;

@Component
public class JobworkCreationValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobworkCreationValidator.class);

	public void validateCuttingQuantityAvailability(Long batchQuantity, CreateCuttingJobworkRequest request,
			Long assignedQuantity, Long repairableDamages) {

		LOGGER.debug("Assigned quantities {} for batch", assignedQuantity);
		LOGGER.debug("Repairable damages quantity {}", repairableDamages);

		boolean isAvailable = (batchQuantity - assignedQuantity + repairableDamages) >= request.getQuantity() ? true
				: false;
		if (!isAvailable) {
			LOGGER.error("All the quantities of the batch has been processed for CUTTING");
			throw new InsufficientBatchQuantityException("No available quantities for the batch");
		}
	}
	
	public void validateItemQuantityAvailability(
	        Long batchItemQuantity,
	        Long assignedQuantity,
	        Long repairableDamages,
	        Long requestedQuantity,
	        String itemName,
	        JobworkType jobworkType
	) {
	    LOGGER.debug("Item {} | Batch qty: {}, Assigned: {}, Repairable: {}, Requested: {}",
	            itemName, batchItemQuantity, assignedQuantity, repairableDamages, requestedQuantity);

	    boolean isAvailable =
	            (batchItemQuantity - assignedQuantity + repairableDamages) >= requestedQuantity;

	    if (!isAvailable) {
	        LOGGER.error("Insufficient quantity for item {} in {}", itemName, jobworkType);
	        throw new InsufficientBatchQuantityException(
	                "Insufficient quantity for item " + itemName
	        );
	    }
	}


}
