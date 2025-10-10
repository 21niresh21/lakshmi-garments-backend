package com.lakshmigarments.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.BatchResponseDTO;
import com.lakshmigarments.dto.BatchResponseDTO.BatchSubCategoryResponseDTO;
import com.lakshmigarments.dto.BatchRequestDTO;
import com.lakshmigarments.dto.BatchSubCategoryRequestDTO;
import com.lakshmigarments.exception.BatchStatusNotFoundException;
import com.lakshmigarments.exception.CategoryNotFoundException;
import com.lakshmigarments.exception.DuplicateBatchException;
import com.lakshmigarments.exception.InsufficientInventoryException;
import com.lakshmigarments.exception.InventoryNotFoundException;
import com.lakshmigarments.exception.SubCategoryNotFoundException;
import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.BatchStatus;
import com.lakshmigarments.model.BatchSubCategory;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.model.Inventory;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.repository.BatchRepository;
import com.lakshmigarments.repository.BatchStatusRepository;
import com.lakshmigarments.repository.BatchSubCategoryRepository;
import com.lakshmigarments.repository.CategoryRepository;
import com.lakshmigarments.repository.SubCategoryRepository;
import com.lakshmigarments.repository.InventoryRepository;

@Service
public class BatchService2 {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchService2.class);
	private final BatchRepository batchRepository;
	private final BatchSubCategoryRepository batchSubCategoryRepository;
	private final CategoryRepository categoryRepository;
	private final SubCategoryRepository subCategoryRepository;
	private final InventoryRepository inventoryRepository;
	private final BatchStatusRepository batchStatusRepository;
	private final ModelMapper modelMapper;

	public BatchService2(BatchRepository batchRepository, BatchSubCategoryRepository batchSubCategoryRepository,
			CategoryRepository categoryRepository, SubCategoryRepository subCategoryRepository,
			InventoryRepository inventoryRepository, BatchStatusRepository batchStatusRepository,
			ModelMapper modelMapper) {
		this.batchRepository = batchRepository;
		this.batchSubCategoryRepository = batchSubCategoryRepository;
		this.categoryRepository = categoryRepository;
		this.subCategoryRepository = subCategoryRepository;
		this.inventoryRepository = inventoryRepository;
		this.batchStatusRepository = batchStatusRepository;
		this.modelMapper = modelMapper;
	}

	// validate duplicate serial code
	private void validateDuplicateSerialCode(String serialCode) {
		if (batchRepository.existsBySerialCode(serialCode)) {
			LOGGER.error("Duplicate serial code: {}", serialCode);
			throw new DuplicateBatchException("Duplicate serial code: " + serialCode);
		}
	}

	private Category validateCategory(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> {
					LOGGER.error("Category not found with ID {}", categoryId);
					return new CategoryNotFoundException("Category not found with ID " + categoryId);
				});
	}

	private BatchStatus validateBatchStatus(Long batchStatusId) {
		return batchStatusRepository.findById(batchStatusId)
				.orElseThrow(() -> {
					LOGGER.error("Batch Status not found with ID {}", batchStatusId);
					return new BatchStatusNotFoundException("Batch Status not found with ID " + batchStatusId);
				});
	}

	private SubCategory validateSubCategory(Long subCategoryId) {
		return subCategoryRepository.findById(subCategoryId)
				.orElseThrow(() -> {
					LOGGER.error("SubCategory not found with ID {}", subCategoryId);
					return new SubCategoryNotFoundException("SubCategory not found with ID " + subCategoryId);
				});
	}

	private Inventory validateInventory(Long categoryId, Long subCategoryId) {
		return inventoryRepository
				.findByCategoryIdAndSubCategoryId(categoryId, subCategoryId)
				.orElseThrow(() -> {
					LOGGER.error("Inventory not found for categoryId {} and subCategoryId {}", categoryId,
							subCategoryId);
					return new InventoryNotFoundException(
							"Inventory not found for categoryId " + categoryId + " and subCategoryId " + subCategoryId);
				});
	}

	private void validateInventoryLevels(Long categoryId, List<BatchSubCategoryRequestDTO> subCategories) {
		for (BatchSubCategoryRequestDTO subCategoryDTO : subCategories) {
			Long subCategoryId = subCategoryDTO.getSubCategoryID();
			Long requestedQty = subCategoryDTO.getQuantity();
	
			Inventory inventory = validateInventory(categoryId, subCategoryId);
			if (inventory.getCount() < requestedQty) {
				throw new InsufficientInventoryException(
					"Not enough inventory for subCategoryId " + subCategoryId +
					". Available: " + inventory.getCount() + ", Requested: " + requestedQty
				);
			}
		}
	}

	public BatchResponseDTO createBatch(BatchRequestDTO createBatchDTO) {

		Long categoryId = createBatchDTO.getCategoryID();
		Long batchStatusId = createBatchDTO.getBatchStatusID();
		validateDuplicateSerialCode(createBatchDTO.getSerialCode());
		validateInventoryLevels(categoryId, createBatchDTO.getSubCategories());

		Category category = validateCategory(categoryId);

		BatchStatus batchStatus = validateBatchStatus(batchStatusId);
		LOGGER.info("BatchStatus: {}", batchStatus);

		// validate the sub category IDs
		for (BatchSubCategoryRequestDTO subCategoryDTO : createBatchDTO.getSubCategories()) {
			Long subCategoryId = subCategoryDTO.getSubCategoryID();
			SubCategory subCategory = validateSubCategory(subCategoryId);
		}

		Batch batch = new Batch();
		batch.setSerialCode(createBatchDTO.getIsUrgent() ? createBatchDTO.getSerialCode() + " (U)"
				: createBatchDTO.getSerialCode());
		batch.setSerialCode(createBatchDTO.getSerialCode());
		batch.setCategory(category);
		batch.setRemarks(createBatchDTO.getRemarks());
		batch.setIsUrgent(createBatchDTO.getIsUrgent());
		batch.setBatchStatus(batchStatus);

		Batch createdBatch = batchRepository.save(batch);

		List<BatchSubCategoryResponseDTO> batchSubCategoryResponseDTOs = new ArrayList<>();

		for (BatchSubCategoryRequestDTO batchSubCategoryDTO : createBatchDTO.getSubCategories()) {

			Long subCategoryId = batchSubCategoryDTO.getSubCategoryID();
			SubCategory subCategory = subCategoryRepository.findById(subCategoryId).get();
			Long quantity = batchSubCategoryDTO.getQuantity();

			// Reduce the count from inventory
			Inventory inventory = validateInventory(categoryId, subCategoryId);
			inventory.setCount(inventory.getCount() - quantity);
			inventoryRepository.save(inventory);

			BatchSubCategory batchSubCategory = new BatchSubCategory();
			batchSubCategory.setBatch(createdBatch);
			batchSubCategory.setQuantity(quantity);
			batchSubCategory.setSubCategory(subCategory);
			BatchSubCategory createdBatchSubCategory = batchSubCategoryRepository.save(batchSubCategory);

			// add for returning results
			batchSubCategoryResponseDTOs
					.add(modelMapper.map(createdBatchSubCategory, BatchSubCategoryResponseDTO.class));
		}

		BatchResponseDTO batchResponseDTO = modelMapper.map(createdBatch, BatchResponseDTO.class);
		batchResponseDTO.setSubCategories(batchSubCategoryResponseDTOs);
		return batchResponseDTO;

	}

	// public List<CreateBatchDTO> getBatches(String search) {
	//
	// List<Batch> batches;
	// if (search != null && !search.isEmpty()) {
	// batches = batchRepository.findBySerialCodeContaining(search);
	// } else {
	// batches = batchRepository.findAll(Sort.by(Sort.Order.desc("createdAt")));
	// }
	//
	// List<CreateBatchDTO> batchDTOs = batches.stream().map(batch -> {
	// List<CreateBatchSubCategoryDTO> batchSubCategoryDTOs =
	// batchSubCategoryRepository.findByBatch(batch)
	// .stream()
	// .map(batchSubCategory -> new CreateBatchSubCategoryDTO(
	// batchSubCategory.getId(),
	// batchSubCategory.getSubCategory().getName(),
	// batchSubCategory.getQuantity()))
	// .collect(Collectors.toList()); // Collect the mapped results into a list
	//
	// return new CreateBatchDTO(batch.getId(), batch.getCategory().getName(),
	// batch.getSerialCode(), batch.getCreatedAt(),
	// batch.getBatchStatus().getName(),
	// batch.getIsUrgent(), batch.getRemarks(), batchSubCategoryDTOs);
	// }).collect(Collectors.toList()); // Collect into a list
	//
	// return batchDTOs;
	// }

}
