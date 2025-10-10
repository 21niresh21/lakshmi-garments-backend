package com.lakshmigarments.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.SubCategoryRequestDTO;
import com.lakshmigarments.dto.SubCategoryResponseDTO;

@Service
public interface SubCategoryService {

    SubCategoryResponseDTO createSubCategory(SubCategoryRequestDTO subCategoryRequestDTO);
    
    SubCategoryResponseDTO updateSubCategory(Long id, SubCategoryRequestDTO subCategoryRequestDTO);
    
    List<SubCategoryResponseDTO> getAllSubCategories(String search);

}
