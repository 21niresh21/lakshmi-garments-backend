package com.lakshmigarments.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.SupplierRequestDTO;
import com.lakshmigarments.dto.SupplierResponseDTO;

@Service
public interface SupplierService {

    SupplierResponseDTO createSupplier(SupplierRequestDTO supplierRequestDTO);

    SupplierResponseDTO updateSupplier(Long id, SupplierRequestDTO supplierRequestDTO);

    List<SupplierResponseDTO> getAllSuppliers(String search);

}
