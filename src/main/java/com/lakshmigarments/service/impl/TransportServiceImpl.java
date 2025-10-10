package com.lakshmigarments.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;

import com.lakshmigarments.dto.TransportRequestDTO;
import com.lakshmigarments.dto.TransportResponseDTO;
import com.lakshmigarments.exception.DuplicateTransportException;
import com.lakshmigarments.exception.TransportNotFoundException;
import com.lakshmigarments.model.Transport;
import com.lakshmigarments.repository.TransportRepository;
import com.lakshmigarments.repository.specification.TransportSpecification;
import com.lakshmigarments.service.TransportService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TransportServiceImpl implements TransportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransportServiceImpl.class);
	private final TransportRepository transportRepository;
	private final ModelMapper modelMapper;

	@Override
	public TransportResponseDTO createTransport(TransportRequestDTO transportRequestDTO) {
		String transportName = transportRequestDTO.getName().trim();

		if (transportRepository.existsByNameIgnoreCase(transportName)) {
			LOGGER.error("Transport already exists with name {}", transportName);
			throw new DuplicateTransportException("Transport already exists with name " + transportName);
		}

		Transport transport = new Transport();
		transport.setName(transportName);

		Transport savedTransport = transportRepository.save(transport);
		LOGGER.debug("Transport created successfully with ID: {}", savedTransport.getId());
		return modelMapper.map(savedTransport, TransportResponseDTO.class);
	}

	@Override
	public List<TransportResponseDTO> getAllTransports(String search) {
		Specification<Transport> spec = TransportSpecification.filterByName(search);

		List<Transport> transports = transportRepository.findAll(spec);

		LOGGER.debug("Found {} transport(s) matching filter", transports.size());

		return transports.stream().map(transport -> modelMapper.map(transport, TransportResponseDTO.class))
				.collect(Collectors.toList());
	}

	@Override
	public TransportResponseDTO updateTransport(Long id, TransportRequestDTO transportRequestDTO) {

		String transportName = transportRequestDTO.getName().trim();

		Transport transport = transportRepository.findById(id).orElseThrow(() -> {
			LOGGER.error("Transport not found with ID: {}", id);
			return new TransportNotFoundException("Transport not found with id: " + id);
		});

		if (transportRepository.existsByNameIgnoreCaseAndIdNot(transportName, id)) {
			LOGGER.error("Transport already exists with name {}", transportName);
			throw new DuplicateTransportException("Transport already exists with name " + transportName);
		}

		transport.setName(transportName);
		Transport updatedTransport = transportRepository.save(transport);

		LOGGER.debug("Transport updated successfully with ID: {}", updatedTransport.getId());

		return modelMapper.map(updatedTransport, TransportResponseDTO.class);
	}

}
