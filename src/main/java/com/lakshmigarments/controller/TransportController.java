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

import com.lakshmigarments.dto.request.TransportRequest;
import com.lakshmigarments.dto.response.TransportResponse;
import com.lakshmigarments.service.TransportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transports")
@RequiredArgsConstructor
public class TransportController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransportController.class);

	private final TransportService transportService;

	@GetMapping
	public ResponseEntity<List<TransportResponse>> getAllTransports(@RequestParam(required = false) String search) {
		LOGGER.info("Received request to fetch all transports matching: {}", search);
		List<TransportResponse> transports = transportService.getAllTransports(search);
		return ResponseEntity.ok(transports);
	}

	@PostMapping
	public ResponseEntity<TransportResponse> createTransport(
			@RequestBody @Valid TransportRequest request) {
		LOGGER.info("Received request to create transport: {}", request.getName());
		TransportResponse response = transportService.createTransport(request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<TransportResponse> updateTransport(@PathVariable Long id,
			@RequestBody @Valid TransportRequest request) {
		LOGGER.info("Received request to update transport ID: {}", id);
		TransportResponse response = transportService.updateTransport(id, request);
		return ResponseEntity.ok(response);
	}

}
