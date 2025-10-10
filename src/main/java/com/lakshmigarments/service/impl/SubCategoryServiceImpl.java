package com.lakshmigarments.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.SubCategoryRequestDTO;
import com.lakshmigarments.dto.SubCategoryResponseDTO;
import com.lakshmigarments.exception.DuplicateSubCategoryException;
import com.lakshmigarments.exception.SubCategoryNotFoundException;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.repository.SubCategoryRepository;
import com.lakshmigarments.repository.specification.SubCategorySpecification;
import com.lakshmigarments.service.SubCategoryService;

import lombok.AllArgsConstructor;

@Service    
@AllArgsConstructor
public class SubCategoryServiceImpl implements SubCategoryService {

    private final Logger LOGGER = LoggerFactory.getLogger(SubCategoryServiceImpl.class);
    private final SubCategoryRepository subCategoryRepository;
    private final ModelMapper modelMapper;
    
    @Override
    public SubCategoryResponseDTO createSubCategory(SubCategoryRequestDTO subCategoryRequestDTO) {

        String subCategoryName = subCategoryRequestDTO.getName().trim();

        if (subCategoryRepository.existsByNameIgnoreCase(subCategoryName)) {
            LOGGER.error("Sub category already exists with name {}", subCategoryName);
            throw new DuplicateSubCategoryException("Sub category already exists with name " + subCategoryName);
        }

        SubCategory subCategory = new SubCategory();
        subCategory.setName(subCategoryName);

        SubCategory savedSubCategory = subCategoryRepository.save(subCategory);
        LOGGER.debug("Sub category created with name {}", savedSubCategory.getName());
        return modelMapper.map(savedSubCategory, SubCategoryResponseDTO.class);
    }

    @Override
    public SubCategoryResponseDTO updateSubCategory(Long id, SubCategoryRequestDTO subCategoryRequestDTO) {
        SubCategory subCategory = subCategoryRepository.findById(id)
            .orElseThrow(() -> new SubCategoryNotFoundException("Sub category not found with id " + id));

        String subCategoryName = subCategoryRequestDTO.getName().trim();

        if (subCategoryRepository.existsByNameIgnoreCaseAndIdNot(subCategoryName, id)) {
            LOGGER.error("Sub category already exists with name {}", subCategoryName);
            throw new DuplicateSubCategoryException("Sub category already exists with name " + subCategoryName);
        }

        subCategory.setName(subCategoryName);

        SubCategory savedSubCategory = subCategoryRepository.save(subCategory);
        return modelMapper.map(savedSubCategory, SubCategoryResponseDTO.class);
    }

    @Override
    public List<SubCategoryResponseDTO> getAllSubCategories(String search) {

        Specification<SubCategory> spec = SubCategorySpecification.filterByName(search);

        List<SubCategory> subCategories = subCategoryRepository.findAll(spec);

        LOGGER.debug("Found {} sub category(s) matching filter", subCategories.size());

        return subCategories.stream()
            .map(subCategory -> modelMapper.map(subCategory, SubCategoryResponseDTO.class))
            .collect(Collectors.toList());
    }
    
}
