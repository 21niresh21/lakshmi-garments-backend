package com.lakshmigarments.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.BatchRequestDTO;
import com.lakshmigarments.dto.BatchUpdateDTO;
import com.lakshmigarments.dto.BatchSubCategoryRequestDTO;
import com.lakshmigarments.dto.BatchResponseDTO;
import com.lakshmigarments.dto.BatchSerialDTO;
import com.lakshmigarments.dto.BatchTimelineDTO;
import com.lakshmigarments.dto.BatchResponseDTO.BatchSubCategoryResponseDTO;
import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.BatchStatus;
import com.lakshmigarments.model.BatchSubCategory;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.model.Damage;
import com.lakshmigarments.model.Inventory;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.model.JobworkType;
import com.lakshmigarments.model.LedgerDirection;
import com.lakshmigarments.model.MaterialInventoryLedger;
import com.lakshmigarments.model.MovementType;
import com.lakshmigarments.model.ReferenceType;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.model.User;
import com.lakshmigarments.exception.BatchNotFoundException;
import com.lakshmigarments.exception.BatchStatusNotFoundException;
import com.lakshmigarments.exception.CategoryNotFoundException;
import com.lakshmigarments.exception.InsufficientInventoryException;
import com.lakshmigarments.exception.InventoryNotFoundException;
import com.lakshmigarments.exception.SubCategoryNotFoundException;
import com.lakshmigarments.exception.UserNotFoundException;
import com.lakshmigarments.repository.BatchRepository;
import com.lakshmigarments.repository.CategoryRepository;
import com.lakshmigarments.repository.BatchSubCategoryRepository;
import com.lakshmigarments.repository.DamageRepository;
import com.lakshmigarments.repository.InventoryRepository;
import com.lakshmigarments.repository.JobworkRepository;
import com.lakshmigarments.repository.MaterialLedgerRepository;
import com.lakshmigarments.repository.SubCategoryRepository;
import com.lakshmigarments.repository.UserRepository;
import com.lakshmigarments.repository.specification.BatchSpecification;
import com.lakshmigarments.service.BatchService;
import com.lakshmigarments.service.EmployeeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

//	private final EmployeeService employeeService;

	private final Logger LOGGER = LoggerFactory.getLogger(BatchServiceImpl.class);
	private final BatchRepository batchRepository;
	private final JobworkRepository jobworkRepository;
	private final BatchSubCategoryRepository batchSubCategoryRepository;
	private final DamageRepository damageRepository;
	private final CategoryRepository categoryRepository;
	private final SubCategoryRepository subCategoryRepository;
	private final InventoryRepository inventoryRepository;
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	private final MaterialLedgerRepository ledgerRepository;

	// BatchServiceImpl(EmployeeService employeeService) {
	// this.employeeService = employeeService;
	// }

	@Override
	@Transactional
	public void createBatch(BatchRequestDTO batchRequestDTO) {

		Category category = categoryRepository.findByName(batchRequestDTO.getCategoryName()).orElseThrow(() -> {
			LOGGER.error("Category not found with name {}", batchRequestDTO.getCategoryName());
			return new CategoryNotFoundException("Category not found with name " + batchRequestDTO.getCategoryName());
		});

		User user = userRepository.findById(batchRequestDTO.getCreatedByID()).orElseThrow(() -> {
			LOGGER.error("User with ID {} not found", batchRequestDTO.getCreatedByID());
			return new UserNotFoundException("User not found with ID " + batchRequestDTO.getCreatedByID());
		});

		BatchStatus batchStatus = batchRequestDTO.getBatchStatus() != null ? batchRequestDTO.getBatchStatus()
				: BatchStatus.CREATED;

		List<BatchSubCategory> batchSubCategories = validateBatchSubCategories(batchRequestDTO.getSubCategories());

		Batch batch = new Batch();
		batch.setCategory(category);
		batch.setBatchStatus(batchStatus);
		batch.setSerialCode(batchRequestDTO.getSerialCode());
		batch.setIsUrgent(batchRequestDTO.getIsUrgent());
		batch.setRemarks(batchRequestDTO.getRemarks());
		batch.setAvailableQuantity(batchRequestDTO.getTotalQuantity());
		batch.setCreatedBy(user);

		Batch createdBatch = batchRepository.save(batch);

		for (BatchSubCategory batchSubCategory : batchSubCategories) {
			batchSubCategory.setBatch(batch);
			batchSubCategory.setAvailableQuantity(batchSubCategory.getQuantity());
			batchSubCategoryRepository.save(batchSubCategory);

			// detect the quantities from inventory
			Long availableQuantity = ledgerRepository.getAvailableQuantityByCategoryAndSubCategory(
					category.getId(), batchSubCategory.getSubCategory().getId());
			if (availableQuantity < batchSubCategory.getQuantity()) {
				throw new InsufficientInventoryException("Stock not available");
			} else {
				MaterialInventoryLedger inventory;
				inventory = new MaterialInventoryLedger();
				inventory.setDirection(LedgerDirection.OUT);
	        	inventory.setMovementType(MovementType.BATCH_CREATION);
	        	inventory.setReferenceType(ReferenceType.BATCH);
	        	inventory.setReference_id(createdBatch.getId());
	        	inventory.setUnit("piece(s)");
				inventory.setQuantity(batchSubCategory.getQuantity());
				inventory.setSubCategory(batchSubCategory.getSubCategory());
				inventory.setCategory(batch.getCategory());
				inventory.setCreatedBy(user);
			
				ledgerRepository.save(inventory);
//				inventory.setCount(inventory.getCount() - batchSubCategory.getQuantity());
//				inventoryRepository.save(inventory);
			}
		}

		return;
	}

	@Override
	public Page<BatchResponseDTO> getAllBatches(Integer pageNo, Integer pageSize, String sortBy, String sortOrder,
			String search, List<String> batchStatusNames, List<String> categoryNames, List<Boolean> isUrgents,
			Date startDate, Date endDate) {

		if (pageNo == null) {
			pageNo = 0;
		}
		if (pageSize == null || pageSize == 0) {
			pageSize = 10;
		}
		System.out.println(sortBy + " " + sortOrder);
		Sort sort = sortOrder.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

		Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

		Specification<Batch> specification = Specification
				.where(BatchSpecification.filterByBatchStatusName(batchStatusNames))
				.and(BatchSpecification.filterByCategoryName(categoryNames))
				.and(BatchSpecification.filterByIsUrgent(isUrgents));

		if (search != null && !search.isEmpty()) {
			Specification<Batch> searchSpecification = Specification.where(null);
			searchSpecification = searchSpecification.or(BatchSpecification.filterBySerialCode(search))
					.or(BatchSpecification.filterByRemarks(search));
			specification = specification.and(searchSpecification);
		}

		if (startDate != null || endDate != null) {
			specification = specification.and(BatchSpecification.filterByDateRange(startDate, endDate));
		}

		Page<Batch> batches = batchRepository.findAll(specification, pageable);

		return batches.map(this::convertToBatchResponseDTO);
	}

	@Override
	public List<BatchSerialDTO> getUnpackagedBatches() {
		LOGGER.info("Fetching unpackaged batches");
		List<Batch> unpackagedBatches = batchRepository.findAllExceptPackagedWithoutRepairableDamages();
		List<BatchSerialDTO> batchSerialDTOs = unpackagedBatches.stream()
				.map(batch -> modelMapper.map(batch, BatchSerialDTO.class)).collect(Collectors.toList());
		LOGGER.info("Found {} unpackaged batches", batchSerialDTOs.size());
		return batchSerialDTOs;
	}

//	@Override
//	public List<BatchTimelineDTO> getBatchTimeline(Long batchId) {
//
//		List<Jobwork> jobworks = jobworkRepository.findByBatchId(batchId);
//		List<BatchTimelineDTO> batchTimelineDTOs = new ArrayList<>();
//
//		if (jobworks.isEmpty()) {
//			LOGGER.info("No jobworks found for batch id: {}", batchId);
//			return new ArrayList<>();	
//		}
//
//		for (Jobwork jobwork : jobworks) {
//			BatchTimelineDTO batchTimelineDTO = new BatchTimelineDTO();
//			batchTimelineDTO.setDateTime(jobwork.getStartedAt());
//			batchTimelineDTO.setJobworkType(jobwork.getJobworkType());
//			if (jobwork.getJobworkType() == JobworkType.CUTTING) {
//				String description = "Assigned " + jobwork.getQuantity() + " pieces to "
//						+ jobwork.getEmployee().getName();
//				batchTimelineDTO.setDescription(description);
//			} else {
//				String description = "Assigned " + jobwork.getQuantity() + " of item " + jobwork.getItem().getName()
//						+ " to " + jobwork.getEmployee().getName();
//				batchTimelineDTO.setDescription(description);
//			}
//			batchTimelineDTO.setJobworkNumber(jobwork.getJobworkNumber());
//			batchTimelineDTOs.add(batchTimelineDTO);
//
//			if (jobwork.getEndedAt() != null) {
//				BatchTimelineDTO batchTimelineDTOForEnd = new BatchTimelineDTO();
//				batchTimelineDTO.setDateTime(jobwork.getEndedAt());
//				batchTimelineDTO.setJobworkType(jobwork.getJobworkType());
//				String description = "";
//				if (jobwork.getJobworkType() == JobworkType.CUTTING) {
//					description = "Completed cutting " + jobwork.getQuantity() + " pieces by "
//							+ jobwork.getEmployee().getName();
//				} else {
//					description = "Completed " + jobwork.getQuantity() + " of item " + jobwork.getItem().getName()
//							+ " by " + jobwork.getEmployee().getName();
//				}
//				batchTimelineDTOForEnd.setDescription(description);
//				batchTimelineDTOForEnd.setJobworkNumber(jobwork.getJobworkNumber());
//				batchTimelineDTOs.add(batchTimelineDTOForEnd);
//			}
//
//		}
//
//		return batchTimelineDTOs;
//	}

//	@Override
//	public Long getBatchCount(Long batchId) {
//		List<BatchSubCategory> batchSubCategories = batchSubCategoryRepository.findByBatchId(batchId);
//		System.out.println(batchSubCategories.size());
//
//		List<Damage> damages = damageRepository.findAllByBatchId(batchId);
//		return batchSubCategories.stream().mapToLong(BatchSubCategory::getQuantity).sum()
//				- damages.stream().mapToLong(Damage::getQuantity).sum();
//	}

	private List<BatchSubCategory> validateBatchSubCategories(List<BatchSubCategoryRequestDTO> batchSubCategories) {
		List<BatchSubCategory> validatedBatchSubCategories = new ArrayList<>();
		for (BatchSubCategoryRequestDTO batchSubCategoryRequestDTO : batchSubCategories) {
			SubCategory subCategory = subCategoryRepository.findByName(batchSubCategoryRequestDTO.getSubCategoryName())
					.orElseThrow(() -> {
						LOGGER.error("Sub category not found with id {}",
								batchSubCategoryRequestDTO.getSubCategoryName());
						return new SubCategoryNotFoundException(
								"Sub category not found with name " + batchSubCategoryRequestDTO.getSubCategoryName());
					});
			BatchSubCategory batchSubCategory = new BatchSubCategory();
			batchSubCategory.setSubCategory(subCategory);
			batchSubCategory.setQuantity(batchSubCategoryRequestDTO.getQuantity());
			validatedBatchSubCategories.add(batchSubCategory);
		}
		return validatedBatchSubCategories;
	}

	// map the subcategories to the batch response dto
	private BatchResponseDTO convertToBatchResponseDTO(Batch batch) {
		BatchResponseDTO batchResponseDTO = modelMapper.map(batch, BatchResponseDTO.class);
		List<BatchSubCategory> batchSubCategories = batchSubCategoryRepository.findByBatchId(batch.getId());
		List<BatchSubCategoryResponseDTO> batchSubCategoryResponseDTOs = batchSubCategories.stream()
				.map(batchSubCategory -> modelMapper.map(batchSubCategory, BatchSubCategoryResponseDTO.class))
				.collect(Collectors.toList());
		batchResponseDTO.setSubCategories(batchSubCategoryResponseDTOs);
		return batchResponseDTO;
	}

	@Override
	@Transactional
	public void updateBatch(Long batchId, BatchUpdateDTO batchUpdateDTO) {
		Batch batch = batchRepository.findById(batchId).orElseThrow(() -> {
			LOGGER.error("Batch not found with id {}", batchId);
			return new BatchNotFoundException("Batch not found with id " + batchId);
		});

		if (batchUpdateDTO.getSerialCode() != null) {
			batch.setSerialCode(batchUpdateDTO.getSerialCode());
		}
		if (batchUpdateDTO.getCategoryName() != null) {
			Category category = categoryRepository.findByName(batchUpdateDTO.getCategoryName()).orElseThrow(() -> {
				LOGGER.error("Category not found with name {}", batchUpdateDTO.getCategoryName());
				return new CategoryNotFoundException(
						"Category not found with name " + batchUpdateDTO.getCategoryName());
			});
			batch.setCategory(category);
		}
		if (batchUpdateDTO.getIsUrgent() != null) {
			batch.setIsUrgent(batchUpdateDTO.getIsUrgent());
		}
		if (batchUpdateDTO.getRemarks() != null) {
			batch.setRemarks(batchUpdateDTO.getRemarks());
		}
		if (batchUpdateDTO.getSubCategories() != null) {
			List<BatchSubCategory> batchSubCategories = validateBatchSubCategories(batchUpdateDTO.getSubCategories());
			for (BatchSubCategory batchSubCategory : batchSubCategories) {
				batchSubCategory.setBatch(batch);
				batchSubCategoryRepository.save(batchSubCategory);
			}
		}
		if (batchUpdateDTO.getBatchStatusName() != null) {
			try {
				BatchStatus batchStatus = BatchStatus.valueOf(batchUpdateDTO.getBatchStatusName().toUpperCase());

				if (batchStatus == BatchStatus.DISCARDED) {
					batch.setBatchStatus(batchStatus);
				}

			} catch (IllegalArgumentException ex) {
				LOGGER.error("Invalid batch status {}", batchUpdateDTO.getBatchStatusName());
				throw new BatchStatusNotFoundException(
						"Batch status not found with name " + batchUpdateDTO.getBatchStatusName());
			}
		}

		batchRepository.save(batch);
	}

	// TODO
	@Override
	public List<JobworkType> getJobworkTypes(String batchSerialCode) {
		List<JobworkType> result = new ArrayList<>();
		Batch batch = batchRepository.findBySerialCode(batchSerialCode).orElseThrow(() -> {
			LOGGER.error("Batch not found with serial code {}", batchSerialCode);
			return new BatchNotFoundException("Batch not found with serial code " + batchSerialCode);
		});

		Long remainingUnits = batchSubCategoryRepository.findRemainingUnitsInBatch(batch.getId());
//		result.add(JobworkType.CUTTING);

		result.add(JobworkType.CUTTING);
		result.add(JobworkType.STITCHING);
		return result;
	}

	// mark the batch as discarded in batch status and refill inventory
	@Override
	public void recycleBatch(Long batchId) {
		Batch batch = batchRepository.findById(batchId).orElseThrow(() -> {
			LOGGER.error("Batch not found with id {}", batchId);
			return new BatchNotFoundException("Batch not found with id " + batchId);
		});
		
		if (batch.getBatchStatus() == BatchStatus.DISCARDED) {
			LOGGER.info("Batch already recyles");
			return;
		}

		List<BatchSubCategory> batchSubCategories = batchSubCategoryRepository.findByBatchId(batchId);

		long categoryId = batch.getCategory().getId();

		List<Inventory> validInventories = new ArrayList<>();
		for (BatchSubCategory batchSubCategory : batchSubCategories) {
			batchSubCategory.setAvailableQuantity(0L);
//			boolean isInventoryValid = inventoryRepository.existsByCategoryIdAndSubCategoryId(categoryId,
//					batchSubCategory.getSubCategory().getId());
//			if (isInventoryValid) {
//				Inventory inventory = inventoryRepository
//						.findByCategoryIdAndSubCategoryId(categoryId, batchSubCategory.getSubCategory().getId())
//						.orElseThrow(() -> {
//							LOGGER.error("Inventory not found with category ID {}", categoryId);
//							return new InventoryNotFoundException("Inventory not found with category id " + categoryId);
//						});
//				long countAfterRecycle = inventory.getCount() + batchSubCategory.getQuantity();
//				inventory.setCount(countAfterRecycle);
//				validInventories.add(inventory);
//			}
			MaterialInventoryLedger inventory;
			inventory = new MaterialInventoryLedger();
			inventory.setDirection(LedgerDirection.IN);
        	inventory.setMovementType(MovementType.BATCH_RECYLE);
        	inventory.setReferenceType(ReferenceType.BATCH);
        	inventory.setReference_id(batch.getId());
        	inventory.setUnit("piece(s)");
			inventory.setQuantity(batchSubCategory.getQuantity());
			inventory.setSubCategory(batchSubCategory.getSubCategory());
			inventory.setCategory(batch.getCategory());
//			inventory.setCreatedBy(user);
		
			ledgerRepository.save(inventory);
		}		
		for (Inventory inventory : validInventories) {
			inventoryRepository.save(inventory);
		}
		batch.setBatchStatus(BatchStatus.DISCARDED);
		batch.setAvailableQuantity(0L);
		batchRepository.save(batch);
	}

}
