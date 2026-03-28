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

import com.lakshmigarments.dto.request.SupplierRequest;
import com.lakshmigarments.dto.response.SupplierResponse;
import com.lakshmigarments.service.SupplierService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for managing supplier operations.
 * Provides endpoints for creating, retrieving, and updating suppliers.
 */
@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SupplierController.class);

	private final SupplierService supplierService;

	/**
	 * Retrieves all suppliers, optionally filtered by name search term.
	 *
	 * @param search Optional search term to filter suppliers by name
	 * @return List of supplier responses
	 */
	@GetMapping
	public ResponseEntity<List<SupplierResponse>> getAllSuppliers(@RequestParam(required = false) String search) {
		LOGGER.info("Received request to fetch all suppliers matching: {}", search);
		List<SupplierResponse> suppliers = supplierService.getSuppliers(search);
		return ResponseEntity.ok(suppliers);
	}

	/**
	 * Creates a new supplier with the provided details.
	 *
	 * @param request Validated supplier request containing name and location
	 * @return Created supplier response with HttpStatus.CREATED
	 */
	@PostMapping
	public ResponseEntity<SupplierResponse> createSupplier(
			@RequestBody @Valid SupplierRequest request) {
		LOGGER.info("Received request to create supplier: {}", request.getName());
		SupplierResponse response = supplierService.createSupplier(request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	/**
	 * Updates an existing supplier identified by ID with the provided details.
	 *
	 * @param id Supplier ID to update
	 * @param request Validated supplier request containing updated name and location
	 * @return Updated supplier response
	 */
	@PutMapping("/{id}")
	public ResponseEntity<SupplierResponse> updateSupplier(@PathVariable Long id,
			@RequestBody @Valid SupplierRequest request) {
		LOGGER.info("Received request to update supplier ID: {}", id);
		SupplierResponse response = supplierService.updateSupplier(id, request);
		return ResponseEntity.ok(response);
	}

}
