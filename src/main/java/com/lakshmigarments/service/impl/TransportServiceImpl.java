package com.lakshmigarments.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lakshmigarments.dto.request.TransportRequest;
import com.lakshmigarments.dto.response.TransportResponse;
import com.lakshmigarments.exception.DuplicateTransportException;
import com.lakshmigarments.exception.TransportNotFoundException;
import com.lakshmigarments.model.Transport;
import com.lakshmigarments.repository.TransportRepository;
import com.lakshmigarments.repository.specification.TransportSpecification;
import com.lakshmigarments.service.TransportService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransportServiceImpl implements TransportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransportServiceImpl.class);

	private final TransportRepository transportRepository;
	private final ModelMapper modelMapper;

	@Override
	@Transactional(readOnly = true)
	public List<TransportResponse> getAllTransports(String search) {
		LOGGER.debug("Fetching all transports matching: {}", search);
		Specification<Transport> spec = TransportSpecification.filterByName(search);
		List<Transport> transports = transportRepository.findAll(spec);

		LOGGER.debug("Found {} transport(s) matching filter", transports.size());
		return transports.stream()
				.map(transport -> modelMapper.map(transport, TransportResponse.class))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public TransportResponse createTransport(TransportRequest transportRequest) {
		LOGGER.debug("Creating transport: {}", transportRequest.getName());
		String transportName = transportRequest.getName().trim();

		validateTransportUniqueness(transportName, null);

		Transport transport = new Transport();
		transport.setName(transportName);

		Transport savedTransport = transportRepository.save(transport);
		LOGGER.info("Transport created successfully with ID: {}", savedTransport.getId());
		return modelMapper.map(savedTransport, TransportResponse.class);
	}

	@Override
	@Transactional
	public TransportResponse updateTransport(Long id, TransportRequest transportRequest) {
		LOGGER.debug("Updating transport with ID: {}", id);
		
		Transport transport = this.getTransportOrThrow(id);
		
		String transportName = transportRequest.getName().trim();

		validateTransportUniqueness(transportName, id);

		transport.setName(transportName);
		
		Transport updatedTransport = transportRepository.save(transport);
		LOGGER.info("Transport updated successfully with ID: {}", updatedTransport.getId());
		return modelMapper.map(updatedTransport, TransportResponse.class);
	}

	private void validateTransportUniqueness(String name, Long id) {
		if (id == null) {
			if (transportRepository.existsByNameIgnoreCase(name)) {
				LOGGER.error("Transport name already exists: {}", name);
				throw new DuplicateTransportException("Transport already exists with name: " + name);
			}
		} else {
			if (transportRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
				LOGGER.error("Transport name already exists for another ID: {}", name);
				throw new DuplicateTransportException("Transport already exists with name: " + name);
			}
		}
	}

	private Transport getTransportOrThrow(Long id) {
		return transportRepository.findById(id).orElseThrow(() -> {
			LOGGER.error("Transport not found with ID: {}", id);
			return new TransportNotFoundException("Transport not found with ID: " + id);
		});
	}

}
