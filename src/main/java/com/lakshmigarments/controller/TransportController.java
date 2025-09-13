package com.lakshmigarments.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.CreateTransportDTO;
import com.lakshmigarments.dto.UpdateTransportDTO;
import com.lakshmigarments.model.Transport;
import com.lakshmigarments.service.TransportService;

@RestController
@RequestMapping("/transports")
@CrossOrigin(origins = "*")
public class TransportController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransportController.class);
	private final TransportService transportService;
	
	public TransportController(TransportService transportService) {
		this.transportService = transportService;
	}
	
	@PostMapping
	public ResponseEntity<Transport> createTransport(@RequestBody @Validated CreateTransportDTO createTransportDTO) {
		LOGGER.info("Create a new transport");
		return new ResponseEntity<>(transportService.createTransport(createTransportDTO), HttpStatus.CREATED);
	}
	
	@GetMapping
	public ResponseEntity<Page<Transport>> getTransports(
			@RequestParam(defaultValue = "0", required = false) Integer pageNo,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "id", required = false) String sortBy,
			@RequestParam(defaultValue = "asc", required = false) String sortDir) {
		Page<Transport> transportPage = transportService.getTransports(pageNo, pageSize, sortBy, sortDir, search);
		LOGGER.info("Retrieve transports");
		return new ResponseEntity<Page<Transport>>(transportPage, HttpStatus.OK);
	}
	
	@PatchMapping("/{id}")
	public ResponseEntity<Transport> updateTransport(
	        @PathVariable Long id,
	        @RequestBody @Validated UpdateTransportDTO updateTransportDTO) {

	    LOGGER.info("Updating transport with ID: {}", id);
	    Transport updatedTransport = transportService.updateTransport(id, updateTransportDTO);
	    LOGGER.info("Updated transport with ID: {}", id);
	    
	    return ResponseEntity.ok(updatedTransport);
	}

}
