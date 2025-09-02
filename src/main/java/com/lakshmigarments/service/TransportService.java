package com.lakshmigarments.service;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.CreateTransportDTO;
import com.lakshmigarments.exception.DuplicateTransportException;
import com.lakshmigarments.model.Transport;
import com.lakshmigarments.repository.TransportRepository;

@Service
public class TransportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransportService.class);
	private final TransportRepository transportRepository;
	private final ModelMapper modelMapper;

	public TransportService(TransportRepository transportRepository, ModelMapper modelMapper) {
		this.transportRepository = transportRepository;
		this.modelMapper = modelMapper;
	}

	public Transport createTransport(CreateTransportDTO createTransportDTO) {

		if (transportRepository.existsByName(createTransportDTO.getName())) {
			LOGGER.error("Failed to create tranport: Name already exists");
			throw new DuplicateTransportException("Transport with the same name already exists");
		}
		Transport transport = new Transport();
		transport.setName(createTransportDTO.getName());
		Transport createdTransport = transportRepository.save(transport);
		LOGGER.info("Created transport with name {}", createdTransport.getName());
		return createdTransport;

	}

	public Page<Transport> getTransports(Integer pageNo, Integer pageSize, String sortBy, String sortDir) {

		if (pageSize == null) {
			LOGGER.info("Retrieved all transports");
			Pageable wholePage = Pageable.unpaged();
			return transportRepository.findAll(wholePage);
		}

		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
				: Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
		Page<Transport> transportPage = transportRepository.findAll(pageable);

		LOGGER.info("Retrieved transports as pages");
		return transportPage;
	}
}
