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
import com.lakshmigarments.exception.DuplicateSupplierException;
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
	
	public Page<Supplier> getSuppliers(Integer pageNo, Integer pageSize, String sortBy, String sortDir) {
		
		if (pageSize == null) {
			LOGGER.info("Retrieved all suppliers");
			Pageable wholePage = Pageable.unpaged();
			return supplierRepository.findAll(wholePage);
		}
		
		Sort sort  = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
						? Sort.by(sortBy).ascending()
						: Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
		Page<Supplier> supplierPage = supplierRepository.findAll(pageable);
		
		LOGGER.info("Retrieved suppliers as pages");
		return supplierPage;
	}
}
