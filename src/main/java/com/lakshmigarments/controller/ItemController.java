package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.request.ItemRequest;
import com.lakshmigarments.dto.response.ItemResponse;
import com.lakshmigarments.service.ItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemController.class);

	private final ItemService itemService;

	@GetMapping
	public ResponseEntity<List<ItemResponse>> getAllItems(@RequestParam(required = false) String search) {
		LOGGER.info("Received request to fetch all items matching: {}", search);
		List<ItemResponse> items = itemService.getAllItems(search);
		return ResponseEntity.ok(items);
	}

	@PostMapping
	public ResponseEntity<ItemResponse> createItem(
			@RequestBody @Valid ItemRequest request) {
		LOGGER.info("Received request to create item: {}", request.getName());
		ItemResponse response = itemService.createItem(request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ItemResponse> updateItem(@PathVariable Long id,
			@RequestBody @Valid ItemRequest request) {
		LOGGER.info("Received request to update item ID: {}", id);
		ItemResponse response = itemService.updateItem(id, request);
		return ResponseEntity.ok(response);
	}

}
