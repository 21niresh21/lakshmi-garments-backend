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
import com.lakshmigarments.model.BatchStatusEnum;
import com.lakshmigarments.model.BatchSubCategory;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.model.Damage;
import com.lakshmigarments.model.Inventory;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.exception.BatchNotFoundException;
import com.lakshmigarments.exception.BatchStatusNotFoundException;
import com.lakshmigarments.exception.CategoryNotFoundException;
import com.lakshmigarments.exception.InsufficientInventoryException;
import com.lakshmigarments.exception.SubCategoryNotFoundException;
import com.lakshmigarments.repository.BatchRepository;
import com.lakshmigarments.repository.BatchStatusRepository;
import com.lakshmigarments.repository.CategoryRepository;
import com.lakshmigarments.repository.BatchSubCategoryRepository;
import com.lakshmigarments.repository.DamageRepository;
import com.lakshmigarments.repository.InventoryRepository;
import com.lakshmigarments.repository.JobworkRepository;
import com.lakshmigarments.repository.SubCategoryRepository;
import com.lakshmigarments.repository.specification.BatchSpecification;
import com.lakshmigarments.service.BatchService;
import com.lakshmigarments.service.EmployeeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final EmployeeService employeeService;

    private final Logger LOGGER = LoggerFactory.getLogger(BatchServiceImpl.class);
    private final BatchRepository batchRepository;
    private final JobworkRepository jobworkRepository;
    private final BatchSubCategoryRepository batchSubCategoryRepository;
    private final DamageRepository damageRepository;
    private final CategoryRepository categoryRepository;
    private final BatchStatusRepository batchStatusRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;

    // BatchServiceImpl(EmployeeService employeeService) {
    // this.employeeService = employeeService;
    // }

    @Override
    @Transactional
    public void createBatch(BatchRequestDTO batchRequestDTO) {

        Category category = categoryRepository.findById(batchRequestDTO.getCategoryID())
                .orElseThrow(() -> {
                    LOGGER.error("Category not found with id {}", batchRequestDTO.getCategoryID());
                    return new CategoryNotFoundException(
                            "Category not found with id " + batchRequestDTO.getCategoryID());
                });

        BatchStatus batchStatus = batchStatusRepository.findByName(BatchStatusEnum.CREATED.getValue()).orElse(null);
        if (batchRequestDTO.getBatchStatusID() != null) {
            batchStatus = batchStatusRepository.findById(batchRequestDTO.getBatchStatusID())
                    .orElseThrow(() -> {
                        LOGGER.error("Batch status not found with id {}", batchRequestDTO.getBatchStatusID());
                        return new BatchStatusNotFoundException(
                                "Batch status not found with id " + batchRequestDTO.getBatchStatusID());
                    });
        }

        List<BatchSubCategory> batchSubCategories = validateBatchSubCategories(batchRequestDTO.getSubCategories());

        Batch batch = new Batch();
        batch.setCategory(category);
        batch.setBatchStatus(batchStatus);
        batch.setSerialCode(batchRequestDTO.getSerialCode());
        batch.setIsUrgent(batchRequestDTO.getIsUrgent());
        batch.setRemarks(batchRequestDTO.getRemarks());

        batchRepository.save(batch);

        for (BatchSubCategory batchSubCategory : batchSubCategories) {
            batchSubCategory.setBatch(batch);
            batchSubCategoryRepository.save(batchSubCategory);

            // detect the quantities from inventory
            Inventory inventory = inventoryRepository
                    .findBySubCategoryNameAndCategoryName(batchSubCategory.getSubCategory().getName(),
                            category.getName())
                    .orElse(null);
            if (inventory.getCount() < batchSubCategory.getQuantity()) {
                throw new InsufficientInventoryException("Stock not available");
            } else {
                inventory.setCount(inventory.getCount() - batchSubCategory.getQuantity());
                inventoryRepository.save(inventory);
            }
        }

        return;
    }

    @Override
    public Page<BatchResponseDTO> getAllBatches(Integer pageNo, Integer pageSize, String sortBy,
            String sortOrder, String search, List<String> batchStatusNames, List<String> categoryNames,
            List<Boolean> isUrgents, Date startDate, Date endDate) {

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
        List<Batch> unpackagedBatches = batchRepository.findUnpackagedBatches();
        List<BatchSerialDTO> batchSerialDTOs = unpackagedBatches.stream()
                .map(batch -> modelMapper.map(batch, BatchSerialDTO.class))
                .collect(Collectors.toList());
        LOGGER.info("Found {} unpackaged batches", batchSerialDTOs.size());
        return batchSerialDTOs;
    }

    @Override
    public List<BatchTimelineDTO> getBatchTimeline(Long batchId) {

        List<Jobwork> jobworks = jobworkRepository.findByBatchId(batchId);
        List<BatchTimelineDTO> batchTimelineDTOs = new ArrayList<>();

        if (jobworks.isEmpty()) {
            LOGGER.info("No jobworks found for batch id: {}", batchId);
            return new ArrayList<>();
        }

        for (Jobwork jobwork : jobworks) {
            BatchTimelineDTO batchTimelineDTO = new BatchTimelineDTO();
            batchTimelineDTO.setDateTime(jobwork.getStartedAt());
            batchTimelineDTO.setJobworkType(jobwork.getJobworkType().getName());
            if (jobwork.getJobworkType().getName().equals("Cutting")) {
                String description = "Assigned " + jobwork.getQuantity() + " pieces to "
                        + jobwork.getEmployee().getName();
                batchTimelineDTO.setDescription(description);
            } else {
                String description = "Assigned " + jobwork.getQuantity() + " of item " + jobwork.getItem().getName()
                        + " to " + jobwork.getEmployee().getName();
                batchTimelineDTO.setDescription(description);
            }
            batchTimelineDTO.setJobworkNumber(jobwork.getJobworkNumber());
            batchTimelineDTOs.add(batchTimelineDTO);

            if (jobwork.getEndedAt() != null) {
                BatchTimelineDTO batchTimelineDTOForEnd = new BatchTimelineDTO();
                batchTimelineDTO.setDateTime(jobwork.getEndedAt());
                batchTimelineDTO.setJobworkType(jobwork.getJobworkType().getName());
                String description = "";
                if (jobwork.getJobworkType().getName().equals("Cutting")) {
                    description = "Completed cutting " + jobwork.getQuantity() + " pieces by "
                            + jobwork.getEmployee().getName();
                } else {
                    description = "Completed " + jobwork.getQuantity() + " of item " + jobwork.getItem().getName()
                            + " by " + jobwork.getEmployee().getName();
                }
                batchTimelineDTOForEnd.setDescription(description);
                batchTimelineDTOForEnd.setJobworkNumber(jobwork.getJobworkNumber());
                batchTimelineDTOs.add(batchTimelineDTOForEnd);
            }

        }

        return batchTimelineDTOs;
    }

    @Override
    public Long getBatchCount(Long batchId) {
        List<BatchSubCategory> batchSubCategories = batchSubCategoryRepository.findByBatchId(batchId);
        System.out.println(batchSubCategories.size());

        List<Damage> damages = damageRepository.findAllByBatchId(batchId);
        return batchSubCategories.stream().mapToLong(BatchSubCategory::getQuantity).sum()
                - damages.stream().mapToLong(Damage::getQuantity).sum();
    }

    private List<BatchSubCategory> validateBatchSubCategories(List<BatchSubCategoryRequestDTO> batchSubCategories) {
        List<BatchSubCategory> validatedBatchSubCategories = new ArrayList<>();
        for (BatchSubCategoryRequestDTO batchSubCategoryRequestDTO : batchSubCategories) {
            SubCategory subCategory = subCategoryRepository.findById(batchSubCategoryRequestDTO.getSubCategoryID())
                    .orElseThrow(() -> {
                        LOGGER.error("Sub category not found with id {}",
                                batchSubCategoryRequestDTO.getSubCategoryID());
                        return new SubCategoryNotFoundException(
                                "Sub category not found with id " + batchSubCategoryRequestDTO.getSubCategoryID());
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
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> {
                    LOGGER.error("Batch not found with id {}", batchId);
                    return new BatchNotFoundException("Batch not found with id " + batchId);
                });

        if (batchUpdateDTO.getSerialCode() != null) {
            batch.setSerialCode(batchUpdateDTO.getSerialCode());
        }
        if (batchUpdateDTO.getCategoryName() != null) {
            Category category = categoryRepository.findByName(batchUpdateDTO.getCategoryName())
                    .orElseThrow(() -> {
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
            BatchStatus batchStatus = batchStatusRepository.findByName(batchUpdateDTO.getBatchStatusName())
                    .orElseThrow(() -> {
                        LOGGER.error("Batch status not found with name {}", batchUpdateDTO.getBatchStatusName());
                        return new BatchStatusNotFoundException(
                                "Batch status not found with name " + batchUpdateDTO.getBatchStatusName());
                    });
            if (batchStatus.getName().equals(BatchStatusEnum.DISCARDED.getValue())) {
            batch.setBatchStatus(batchStatus);
        }
        batchRepository.save(batch);
    }
    }
}
