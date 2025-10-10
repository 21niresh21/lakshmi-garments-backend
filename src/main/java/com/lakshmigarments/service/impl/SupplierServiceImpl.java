package com.lakshmigarments.service.impl;


import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;

import com.lakshmigarments.dto.SupplierRequestDTO;
import com.lakshmigarments.dto.SupplierResponseDTO;
import com.lakshmigarments.model.Supplier;
import com.lakshmigarments.exception.DuplicateSupplierException;
import com.lakshmigarments.exception.SupplierNotFoundException;
import com.lakshmigarments.repository.SupplierRepository;
import com.lakshmigarments.service.SupplierService;

import lombok.AllArgsConstructor;

import com.lakshmigarments.repository.specification.SupplierSpecification;

@Service
@AllArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupplierServiceImpl.class);

    private final SupplierRepository supplierRepository;

    private final ModelMapper modelMapper;

    @Override
    public SupplierResponseDTO createSupplier(SupplierRequestDTO supplierRequestDTO) {
        String supplierName = supplierRequestDTO.getName().trim();
        String supplierLocation = supplierRequestDTO.getLocation().trim();

        if (supplierRepository.existsByNameIgnoreCase(supplierName)) {
            LOGGER.error("Supplier already exists with name {}", supplierName);
            throw new DuplicateSupplierException("Supplier already exists with name " + supplierName);
        }

        Supplier supplier = new Supplier();
        supplier.setName(supplierName);
        supplier.setLocation(supplierLocation);

        Supplier savedSupplier = supplierRepository.save(supplier);
        LOGGER.debug("Supplier created with name {}", savedSupplier.getName());
        return modelMapper.map(savedSupplier, SupplierResponseDTO.class);
    }

    @Override
    public SupplierResponseDTO updateSupplier(Long id, SupplierRequestDTO supplierRequestDTO) {

        String supplierName = supplierRequestDTO.getName().trim();
        String supplierLocation = supplierRequestDTO.getLocation().trim();

        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> {
                LOGGER.error("Supplier not found with ID: {}", id);
                return new SupplierNotFoundException("Supplier not found with ID: " + id);
            });

        if (supplierRepository.existsByNameIgnoreCaseAndIdNot(supplierName, id)) {
            LOGGER.error("Supplier already exists with name {}", supplierName);
            throw new DuplicateSupplierException("Supplier already exists with name " + supplierName);
        }

        supplier.setName(supplierName);
        supplier.setLocation(supplierLocation);

        Supplier updatedSupplier = supplierRepository.save(supplier);
        LOGGER.debug("Supplier updated with name {}", updatedSupplier.getName());
        return modelMapper.map(updatedSupplier, SupplierResponseDTO.class);
    }

    @Override
    public List<SupplierResponseDTO> getAllSuppliers(String search) {

        Specification<Supplier> spec = SupplierSpecification.filterByName(search);

        List<Supplier> suppliers = supplierRepository.findAll(spec);

        LOGGER.debug("Found {} supplier(s) matching filter", suppliers.size());

        return suppliers.stream().map(supplier -> modelMapper.map(supplier, SupplierResponseDTO.class)).collect(Collectors.toList());

    }

}
