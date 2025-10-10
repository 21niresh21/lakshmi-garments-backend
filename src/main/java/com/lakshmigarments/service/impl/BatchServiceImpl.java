package com.lakshmigarments.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.BatchRequestDTO;
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
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.exception.BatchStatusNotFoundException;
import com.lakshmigarments.exception.CategoryNotFoundException;
import com.lakshmigarments.exception.SubCategoryNotFoundException;
import com.lakshmigarments.repository.BatchRepository;
import com.lakshmigarments.repository.BatchStatusRepository;
import com.lakshmigarments.repository.CategoryRepository;
import com.lakshmigarments.repository.BatchSubCategoryRepository;
import com.lakshmigarments.repository.DamageRepository;
import com.lakshmigarments.repository.JobworkRepository;
import com.lakshmigarments.repository.SubCategoryRepository;
import com.lakshmigarments.service.BatchService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final Logger LOGGER = LoggerFactory.getLogger(BatchServiceImpl.class);
    private final BatchRepository batchRepository;
    private final JobworkRepository jobworkRepository;
    private final BatchSubCategoryRepository batchSubCategoryRepository;
    private final DamageRepository damageRepository;
    private final CategoryRepository categoryRepository;
    private final BatchStatusRepository batchStatusRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public void createBatch(BatchRequestDTO batchRequestDTO) {

        Category category = categoryRepository.findById(batchRequestDTO.getCategoryID())
                .orElseThrow(() -> {
                    LOGGER.error("Category not found with id {}", batchRequestDTO.getCategoryID());
                    return new CategoryNotFoundException(
                            "Category not found with id " + batchRequestDTO.getCategoryID());
                });

        BatchStatus batchStatus = batchStatusRepository.findById(batchRequestDTO.getBatchStatusID())
                .orElseThrow(() -> {
                    LOGGER.error("Batch status not found with id {}", batchRequestDTO.getBatchStatusID());
                    return new BatchStatusNotFoundException(
                            "Batch status not found with id " + batchRequestDTO.getBatchStatusID());
                });

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
        }

        return;
    }

    @Override
    public Page<BatchResponseDTO> getAllBatches(Pageable pageable) {
        Page<Batch> batches = batchRepository.findAll(pageable);
        List<BatchResponseDTO> batchResponseDTOs = new ArrayList<>();
        batchResponseDTOs = batches.stream().map(batch -> modelMapper.map(batch, BatchResponseDTO.class))
                .collect(Collectors.toList());

        int i = 0;
        for (Batch batch : batches) {
            List<BatchSubCategory> batchSubCategories = batchSubCategoryRepository.findByBatchId(batch.getId());
            List<BatchSubCategoryResponseDTO> batchSubCategoryResponseDTOs = batchSubCategories.stream()
                    .map(batchSubCategory -> modelMapper.map(batchSubCategory, BatchSubCategoryResponseDTO.class))
                    .collect(Collectors.toList());
            batchResponseDTOs.get(i).setSubCategories(batchSubCategoryResponseDTOs);
            i++;
        }

        return new PageImpl<>(batchResponseDTOs, pageable, batches.getTotalElements());
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
            batchSubCategory.setQuantity(batchSubCategory.getQuantity());
            validatedBatchSubCategories.add(batchSubCategory);
        }
        return validatedBatchSubCategories;
    }
}
