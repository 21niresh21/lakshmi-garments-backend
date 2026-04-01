package com.lakshmigarments.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lakshmigarments.dto.BatchRequestDTO;
import com.lakshmigarments.dto.BatchSerialDTO;
import com.lakshmigarments.dto.BatchTimelineResponse;
import com.lakshmigarments.model.JobworkType;
import com.lakshmigarments.dto.BatchResponseDTO;
import com.lakshmigarments.service.BatchService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/batches")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BatchController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchController.class);
	private final BatchService batchService;

	@PostMapping
	public ResponseEntity<Void> createBatch(@RequestBody @Valid BatchRequestDTO batchRequestDTO) {
		LOGGER.info("Received request to create a new batch: {}", batchRequestDTO);
		batchService.createBatch(batchRequestDTO);
		LOGGER.info("Batch created successfully");
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<Page<BatchResponseDTO>> getAllBatches(
		@RequestParam(required = false) Integer pageNo,
	@RequestParam(required = false) Integer pageSize,
	@RequestParam(required = false, defaultValue = "isUrgent") String sortBy,
	@RequestParam(required = false, defaultValue = "asc") String sortOrder,
	@RequestParam(required = false) String search,
	@RequestParam(required = false) List<String> batchStatus,
	@RequestParam(required = false) List<String> categoryNames,
	@RequestParam(required = false) List<Boolean> isUrgent,
	@RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
	@RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate
	) {
		LOGGER.info("Received request to get all batches");
		Page<BatchResponseDTO> batchResponseDTOs = batchService.getAllBatches(pageNo, pageSize, sortBy, sortOrder, search, batchStatus, categoryNames, isUrgent, startDate, endDate);
		LOGGER.info("Found {} batches", batchResponseDTOs.getTotalElements());
		return new ResponseEntity<>(batchResponseDTOs, HttpStatus.OK);
	}

//	@GetMapping("/count/{batchId}")
//	public ResponseEntity<Long> getBatchCount(@PathVariable Long batchId) {
//		LOGGER.info("Received request to get batch count for batch id: {}", batchId);
//		Long batchCount = batchService.getBatchCount(batchId);
//		LOGGER.info("Found {} batch count for batch id: {}", batchCount, batchId);
//		return new ResponseEntity<>(batchCount, HttpStatus.OK);
//	}

	// called to for getting the list of batches to assign
	@GetMapping("/pending")
	public ResponseEntity<List<BatchSerialDTO>> getPendingBatches() {
		LOGGER.info("Received request to get pending batches");
		List<BatchSerialDTO> batchSerialDTOs = batchService.getUnpackagedBatches();
		LOGGER.info("Found {} pending batches", batchSerialDTOs.size());
		return new ResponseEntity<>(batchSerialDTOs, HttpStatus.OK);
	}
	
	// gets the possible jobwork types for a batch
	@GetMapping("/{serialCode}/jobwork-types")
	public ResponseEntity<List<JobworkType>> getJobworkTypes(@PathVariable String serialCode) {
		String decodedSerialCode = decodeSerialCode(serialCode);
		LOGGER.info("Received request to fetch the allowed jobwork types for the batch: {}", decodedSerialCode);
		List<JobworkType> allowedJobworkTypes = batchService.getAllowedJobworkTypes(decodedSerialCode);
		return new ResponseEntity<>(allowedJobworkTypes, HttpStatus.OK);
	}

	@GetMapping("/timeline/{batchId}")
	public ResponseEntity<BatchTimelineResponse> getBatchTimeline(@PathVariable Long batchId) {
		LOGGER.info("Received request to get batch timeline for batch id: {}", batchId);
		BatchTimelineResponse batchTimeline = batchService.getBatchTimeline(batchId);
		LOGGER.info("Returning batch timeline for batch id: {}", batchId);
		return ResponseEntity.ok(batchTimeline);
	}
	
	@PostMapping("/recycle/{batchId}")
	public ResponseEntity<Void> recycleBatch(@PathVariable Long batchId) {
		LOGGER.info("Received request to recycle for batch id: {}", batchId);
		batchService.recycleBatch(batchId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@GetMapping("/{serialCode}/{jobworkType}/available-quantity")
	public ResponseEntity<Long> getAvailableQuantity(@PathVariable String serialCode,
			@PathVariable String jobworkType) {
		String decodedSerialCode = decodeSerialCode(serialCode);
		LOGGER.info("Received request for available quantities for batch {} and jobwork type {}", decodedSerialCode, jobworkType);
		Long availableQuantity = batchService.getAvailableQuantities(decodedSerialCode, jobworkType);
		return new ResponseEntity<>(availableQuantity, HttpStatus.OK);
	}
	
	//get the quantity available for cutting for a batch
	@GetMapping("/{serialCode}/cutting/available-quantity")
	public ResponseEntity<Long> getAvailableQuantityForCutting(@PathVariable String serialCode) {
		String decodedSerialCode = decodeSerialCode(serialCode);
		LOGGER.info("Received request for available quantities for cutting work for batch {}", decodedSerialCode);
	    Long availableQuantity = batchService.getAvailableQuantitiesForCutting(decodedSerialCode);
	    return ResponseEntity.ok(availableQuantity);
	}

	@GetMapping("/{serialCode}/cutting/{subCategoryName}/available-quantity")
	public ResponseEntity<Long> getAvailableQuantityBySubCategory(@PathVariable String serialCode,
			@PathVariable String subCategoryName) {
		String decodedSerialCode = decodeSerialCode(serialCode);
		LOGGER.info("Received request for available quantities for sub-category {} for cutting work for batch {}", subCategoryName, decodedSerialCode);
		Long availableQuantity = batchService.getAvailableQuantitiesBySubCategory(decodedSerialCode, subCategoryName);
		return ResponseEntity.ok(availableQuantity);
	}

	// to get the available batches for assigning jobwork
	@GetMapping("/available-for-jobwork")
	public ResponseEntity<List<String>> getBatchesAvailableForJobwork() {
		LOGGER.info("Received request for available batches for jobwork");
	    List<String> batchSerialCodes = batchService.getBatchSerialCodesForJobwork();
	    return ResponseEntity.ok(batchSerialCodes);
	}
	
	@GetMapping("/serial-codes")
	public ResponseEntity<List<String>> getAllBatchSerialCode() {
		LOGGER.info("Received request to get all the batch serial code");
	    List<String> batchSerialCodes = batchService.getAllBatchSerialCode();
	    return ResponseEntity.ok(batchSerialCodes);
	}

	@GetMapping("/serial-code/{serialCode}/sub-categories")
	public ResponseEntity<List<String>> getSubCategoriesBySerialCode(@PathVariable String serialCode) {
		String decodedSerialCode = decodeSerialCode(serialCode);
		LOGGER.info("Received request to get sub-categories for serial code: {}", decodedSerialCode);
		List<String> subCategories = batchService.getSubCategoriesBySerialCode(decodedSerialCode);
		LOGGER.info("Returning sub-categories for serial code: {}", decodedSerialCode);
		return ResponseEntity.ok(subCategories);
	}
	
	/**
	 * Decodes URL-encoded serial codes.
	 * Handles encoded slashes (%2F) in serial codes like "P26%2F27-0001" -> "P26/27-0001"
	 */
	private String decodeSerialCode(String serialCode) {
		if (serialCode == null || serialCode.isEmpty()) {
			return serialCode;
		}
		try {
			return URLDecoder.decode(serialCode, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			LOGGER.error("Failed to decode serial code: {}", serialCode, e);
			return serialCode;
		}
	}
	
}
