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

import com.lakshmigarments.dto.CreateSupplierDTO;
import com.lakshmigarments.dto.UpdateSupplierDTO;
import com.lakshmigarments.exception.DuplicateSupplierException;
import com.lakshmigarments.exception.SupplierNotFoundException;
import com.lakshmigarments.model.Supplier;
import com.lakshmigarments.repository.SupplierRepository;

@Service
public class SupplierService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SupplierService.class);
	private final SupplierRepository supplierRepository;
	private final ModelMapper modelMapper;
	
	public SupplierService(SupplierRepository supplierRepository, ModelMapper modelMapper) {
		this.supplierRepository = supplierRepository;
		this.modelMapper = modelMapper;
	}
	
	public Supplier createSupplier(CreateSupplierDTO createSupplierDTO) {
		try {
	        Supplier supplier = modelMapper.map(createSupplierDTO, Supplier.class);
	        System.out.println(supplier);
	        Supplier createdSupplier = supplierRepository.save(supplier);
	        LOGGER.info("Created supplier with name {}", createdSupplier.getName());
	        return createdSupplier;
	    } catch (DataIntegrityViolationException e) {
	        LOGGER.error("Failed to create supplier: Name already exists");
	        throw new DuplicateSupplierException("Supplier with the same name already exists");
	    }
	}
	
	public Page<Supplier> getSuppliers(Integer pageNo, Integer pageSize, String sortBy, String sortDir, String search) {

	    if (pageSize == null) {
	        LOGGER.info("Retrieved all suppliers");
	        Pageable wholePage = Pageable.unpaged();

	        if (search != null && !search.trim().isEmpty()) {
	            return supplierRepository.findByNameContainingIgnoreCase(search.trim(), wholePage);
	        }

	        return supplierRepository.findAll(wholePage);
	    }

	    Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
	            ? Sort.by(sortBy).ascending()
	            : Sort.by(sortBy).descending();

	    Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

	    if (search != null && !search.trim().isEmpty()) {
	        LOGGER.info("Retrieved suppliers with search: {}", search);
	        return supplierRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
	    }

	    LOGGER.info("Retrieved suppliers as pages");
	    return supplierRepository.findAll(pageable);
	}
	
	public Supplier updateSupplier(Long id, UpdateSupplierDTO dto) {
	    Supplier supplier = supplierRepository.findById(id)
	        .orElseThrow(() -> {
	            LOGGER.error("Supplier not found with ID: {}", id);
	            return new SupplierNotFoundException("Supplier not found with ID: " + id);
	        });

	    if (dto.getName() != null) {
	        boolean nameExists = supplierRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id);
	        if (nameExists) {
	            LOGGER.error("Duplicate supplier name: '{}'", dto.getName());
	            throw new DuplicateSupplierException("Supplier name already exists: " + dto.getName());
	        }
	        supplier.setName(dto.getName());
	    }

	    if (dto.getLocation() != null) {
	        supplier.setLocation(dto.getLocation());
	    }
	    System.out.println(supplier.getName());
	    LOGGER.info("Supplier with ID {} updated successfully", id);
	    return supplierRepository.save(supplier);
	}



}
