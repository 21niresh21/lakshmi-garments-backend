package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.lakshmigarments.dto.BatchResponseDTO;
import com.lakshmigarments.dto.CreateBatchDTO;
import com.lakshmigarments.model.Batch;
import com.lakshmigarments.repository.BatchRepository;
import com.lakshmigarments.service.BatchService;

@RestController
@RequestMapping("/batches")
@CrossOrigin(origins = "*")
public class BatchController {

    private final BatchRepository batchRepository;
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchController.class);
	private final BatchService batchService;
	
	public BatchController(BatchService batchService, BatchRepository batchRepository) {
		this.batchService = batchService;
		this.batchRepository = batchRepository;
	}

	@PostMapping
	public ResponseEntity<BatchResponseDTO> createBatch(@RequestBody @Validated CreateBatchDTO createdBatchDTO) {
	    LOGGER.info("Received request to create a new batch: {}", createdBatchDTO);
	    BatchResponseDTO batchResponseDTO = batchService.createBatch(createdBatchDTO);
	    LOGGER.info("Batch created successfully with id: {}", batchResponseDTO.getId());
	    return new ResponseEntity<>(batchResponseDTO, HttpStatus.CREATED);
	}

	
//	@GetMapping
//	public ResponseEntity<List<CreateBatchDTO>> getBatches(@RequestParam(required = false) String search) {
//		LOGGER.info("Received request to get batches with search param: {}", search);
//		try {
//			List<CreateBatchDTO> batches = batchService.getBatches(search);
//			LOGGER.info("Found {} batches", batches.size());
//			return new ResponseEntity<>(batches, HttpStatus.OK);
//		} catch (Exception e) {
//			LOGGER.error("Error occurred while fetching batches: {}", e.getMessage(), e);
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
}
