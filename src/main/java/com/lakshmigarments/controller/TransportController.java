package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.TransportRequestDTO;
import com.lakshmigarments.dto.TransportResponseDTO;
import com.lakshmigarments.service.TransportService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/transports")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class TransportController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransportController.class);
	private final TransportService transportService;

	@PostMapping
	public ResponseEntity<TransportResponseDTO> createTransport(
			@RequestBody @Valid TransportRequestDTO transportRequestDTO) {
		LOGGER.info("Received request to create a new transport: {}", transportRequestDTO.getName());

		TransportResponseDTO transportResponseDTO = transportService.createTransport(transportRequestDTO);

		LOGGER.info("Transport created successfully with ID: {}", transportResponseDTO.getId());

		return new ResponseEntity<>(transportResponseDTO, HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<TransportResponseDTO>> getAllTransports(@RequestParam(required = false) String search) {
		LOGGER.info("Received request to fetch all transports with search: {}", search);
		List<TransportResponseDTO> transports = transportService.getAllTransports(search);
		LOGGER.info("Returning {} transport(s)", transports.size());
		return ResponseEntity.ok(transports);
	}

	@PutMapping("/{id}")
	public ResponseEntity<TransportResponseDTO> updateTransport(
			@PathVariable Long id,
			@RequestBody @Valid TransportRequestDTO transportRequestDTO) {

		LOGGER.info("Received request to update transport with ID: {}", id);

		TransportResponseDTO updatedTransport = transportService.updateTransport(id, transportRequestDTO);

		LOGGER.info("Transport updated successfully with ID: {}", id);

		return ResponseEntity.ok(updatedTransport);
	}

}
