package com.lakshmigarments.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.response.BatchItemResponse;
import com.lakshmigarments.service.BatchItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/batch-items")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BatchItemController {

	private final Logger LOGGER = LoggerFactory.getLogger(BatchItemController.class);
	private final BatchItemService batchItemService;

	@GetMapping("/batch/{serialCode}/{jobworkType}/available-quantity")
	public ResponseEntity<List<BatchItemResponse>> getItemsByBatchId(@PathVariable String serialCode,
			@PathVariable String jobworkType) {
		String decodedSerialCode = decodeSerialCode(serialCode);
		LOGGER.info("Received request to get available quantity of items for batch serial: {} and jobwork type {}",
				decodedSerialCode, jobworkType);
		List<BatchItemResponse> items = batchItemService.getBatchItemsByBatchSerial(decodedSerialCode, jobworkType);
		LOGGER.info("Found {} items by batch serial code: {}", items.size(), decodedSerialCode);
		return new ResponseEntity<>(items, HttpStatus.OK);
	}

	// get the quantity available for cutting for a batch
//	@GetMapping("/{serialCode}/cutting/available-quantity")
//	public ResponseEntity<Long> getAvailableQuantityForCutting(@PathVariable String serialCode) {
//		LOGGER.info("Received request for available quantities for cutting work for batch {}", serialCode);
//		Long availableQuantity = batchService.getAvailableQuantitiesForCutting(serialCode);
//		return ResponseEntity.ok(availableQuantity);
//	}

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
