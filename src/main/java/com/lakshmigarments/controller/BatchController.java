package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.lakshmigarments.dto.BatchResponseDTO;
import com.lakshmigarments.dto.BatchSerialDTO;
import com.lakshmigarments.dto.CreateBatchDTO;
import com.lakshmigarments.service.BatchService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/batches")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BatchController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchController.class);
	private final BatchService batchService;

	// @PostMapping
	// public ResponseEntity<BatchResponseDTO> createBatch(@RequestBody @Validated
	// CreateBatchDTO createdBatchDTO) {
	// LOGGER.info("Received request to create a new batch: {}", createdBatchDTO);
	// BatchResponseDTO batchResponseDTO =
	// batchService.createBatch(createdBatchDTO);
	// LOGGER.info("Batch created successfully with id: {}",
	// batchResponseDTO.getId());
	// return new ResponseEntity<>(batchResponseDTO, HttpStatus.CREATED);
	// }

	@GetMapping("/pending")
	public ResponseEntity<List<BatchSerialDTO>> getPendingBatches() {
		LOGGER.info("Received request to get pending batches");
		List<BatchSerialDTO> batchSerialDTOs = batchService.getUnpackagedBatches();
		LOGGER.info("Found {} pending batches", batchSerialDTOs.size());
		return new ResponseEntity<>(batchSerialDTOs, HttpStatus.OK);
	}
}
