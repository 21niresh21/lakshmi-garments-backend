package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lakshmigarments.dto.BatchRequestDTO;
import com.lakshmigarments.dto.BatchSerialDTO;
import com.lakshmigarments.dto.BatchTimelineDTO;
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
	public ResponseEntity<Page<BatchResponseDTO>> getAllBatches(Pageable pageable) {
		LOGGER.info("Received request to get all batches");
		Page<BatchResponseDTO> batchResponseDTOs = batchService.getAllBatches(pageable);
		LOGGER.info("Found {} batches", batchResponseDTOs.getTotalElements());
		return new ResponseEntity<>(batchResponseDTOs, HttpStatus.OK);
	}

	@GetMapping("/count/{batchId}")
	public ResponseEntity<Long> getBatchCount(@PathVariable Long batchId) {
		LOGGER.info("Received request to get batch count for batch id: {}", batchId);
		Long batchCount = batchService.getBatchCount(batchId);
		LOGGER.info("Found {} batch count for batch id: {}", batchCount, batchId);
		return new ResponseEntity<>(batchCount, HttpStatus.OK);
	}

	@GetMapping("/pending")
	public ResponseEntity<List<BatchSerialDTO>> getPendingBatches() {
		LOGGER.info("Received request to get pending batches");
		List<BatchSerialDTO> batchSerialDTOs = batchService.getUnpackagedBatches();
		LOGGER.info("Found {} pending batches", batchSerialDTOs.size());
		return new ResponseEntity<>(batchSerialDTOs, HttpStatus.OK);
	}

	@GetMapping("/timeline/{batchId}")
	public ResponseEntity<List<BatchTimelineDTO>> getBatchTimeline(@PathVariable Long batchId) {
		LOGGER.info("Received request to get batch timeline for batch id: {}", batchId);
		List<BatchTimelineDTO> batchTimelineDTOs = batchService.getBatchTimeline(batchId);
		LOGGER.info("Found {} batch timeline for batch id: {}",
				batchTimelineDTOs == null ? 0 : batchTimelineDTOs.size(), batchId);
		return new ResponseEntity<>(batchTimelineDTOs, HttpStatus.OK);
	}
}
