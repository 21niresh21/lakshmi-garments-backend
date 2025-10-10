package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.SupplierRequestDTO;
import com.lakshmigarments.dto.SupplierResponseDTO;
import com.lakshmigarments.service.SupplierService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/suppliers")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class SupplierController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SupplierController.class);

	private final SupplierService supplierService;

	@PostMapping
	public ResponseEntity<SupplierResponseDTO> createSupplier(
			@Valid @RequestBody SupplierRequestDTO supplierRequestDTO) {
		LOGGER.info("Received request to create a new supplier: {}", supplierRequestDTO.getName());

		SupplierResponseDTO supplierResponseDTO = supplierService.createSupplier(supplierRequestDTO);

		LOGGER.info("Supplier created successfully with ID: {}", supplierResponseDTO.getId());

		return ResponseEntity.status(HttpStatus.CREATED).body(supplierResponseDTO);
	}

	@GetMapping
	public ResponseEntity<List<SupplierResponseDTO>> getAllSuppliers(@RequestParam(required = false) String search) {
		LOGGER.info("Received request to fetch all suppliers with search: {}", search);
		List<SupplierResponseDTO> suppliers = supplierService.getAllSuppliers(search);
		LOGGER.info("Returning {} supplier(s)", suppliers.size());
		return ResponseEntity.ok(suppliers);
	}

	@PutMapping("/{id}")
	public ResponseEntity<SupplierResponseDTO> updateSupplier(
			@PathVariable Long id,
			@Valid @RequestBody SupplierRequestDTO supplierRequestDTO) {

		LOGGER.info("Received request to update supplier with ID: {}", id);

		SupplierResponseDTO updatedSupplier = supplierService.updateSupplier(id, supplierRequestDTO);

		LOGGER.info("Supplier updated successfully with ID: {}", id);

		return ResponseEntity.ok(updatedSupplier);
	}

}
