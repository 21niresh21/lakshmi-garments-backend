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

import com.lakshmigarments.dto.CreateSubCategoryDTO;
import com.lakshmigarments.dto.UpdateSubCategoryDTO;
import com.lakshmigarments.exception.CategoryNotFoundException;
import com.lakshmigarments.exception.DuplicateSubCategoryException;
import com.lakshmigarments.exception.SubCategoryNotFoundException;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.repository.CategoryRepository;
import com.lakshmigarments.repository.SubCategoryRepository;

@Service
public class SubCategoryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubCategoryService.class);
	private final SubCategoryRepository subCategoryRepository;
	private final ModelMapper modelMapper;
	private final CategoryRepository categoryRepository;
	
	public SubCategoryService(SubCategoryRepository subCategoryRepository, ModelMapper modelMapper, 
			CategoryRepository categoryRepository) {
		this.subCategoryRepository = subCategoryRepository;
		this.modelMapper = modelMapper;
		this.categoryRepository = categoryRepository;
	}
	
	public SubCategory createSubCategory(CreateSubCategoryDTO createSubCategoryDTO) {
		
		Category category = categoryRepository.findByName(createSubCategoryDTO.getCategoryName()).orElseThrow(() -> {
	        LOGGER.error("Category not found with name {}", createSubCategoryDTO.getCategoryName());
	        return new CategoryNotFoundException("Category not found with name " + createSubCategoryDTO.getCategoryName());
	    });
		
	    try {
	        SubCategory subCategory = modelMapper.map(createSubCategoryDTO, SubCategory.class);
	        subCategory.setCategory(category);
	        SubCategory createdSubCategory = subCategoryRepository.save(subCategory);
	        LOGGER.info("Created sub category with name {}", createdSubCategory.getName());
	        return createdSubCategory;
	    } catch (DataIntegrityViolationException e) {
	        LOGGER.error("Failed to create sub category: Name already exists");
	        throw new DuplicateSubCategoryException("Sub category with the same name already exists");
	    }
	}

	
	public Page<SubCategory> getSubCategories(Integer pageNo, Integer pageSize, String sortBy, String sortDir, String search) {
	    if (pageSize == null) {
	        LOGGER.info("Retrieved all sub categories");
	        Pageable wholePage = Pageable.unpaged();
	        return (search != null && !search.trim().isEmpty())
	                ? subCategoryRepository.findByNameContainingIgnoreCase(search, wholePage)
	                : subCategoryRepository.findAll(wholePage);
	    }

	    Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
	            ? Sort.by(sortBy).ascending()
	            : Sort.by(sortBy).descending();

	    Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

	    Page<SubCategory> subCategoryPage;
	    if (search != null && !search.trim().isEmpty()) {
	        subCategoryPage = subCategoryRepository.findByNameContainingIgnoreCase(search, pageable);
	    } else {
	        subCategoryPage = subCategoryRepository.findAll(pageable);
	    }

	    LOGGER.info("Retrieved sub categories as pages");
	    return subCategoryPage;
	}
	
	public SubCategory updateSubCategory(Long id, UpdateSubCategoryDTO dto) {
	    SubCategory subCategory = subCategoryRepository.findById(id)
	        .orElseThrow(() -> {
	            LOGGER.error("SubCategory not found with ID: {}", id);
	            return new SubCategoryNotFoundException("SubCategory not found with ID: " + id);
	        });

	    String updatedName = dto.getName() != null ? dto.getName() : subCategory.getName();
	    Long updatedCategoryId = dto.getCategoryId() != null ? dto.getCategoryId() : subCategory.getCategory().getId();

	    // Pre-check for duplicate
	    boolean exists = subCategoryRepository.existsByNameIgnoreCaseAndCategoryIdAndIdNot(
	        updatedName,
	        updatedCategoryId,
	        id
	    );

	    if (exists) {
	        LOGGER.error("Duplicate SubCategory with name '{}' in category ID {}", updatedName, updatedCategoryId);
	        throw new DuplicateSubCategoryException("SubCategory with this name already exists in the selected category.");
	    }

	    // Update fields
	    if (dto.getName() != null) {
	        subCategory.setName(dto.getName());
	    }
	    if (dto.getCategoryId() != null) {
	        Category category = categoryRepository.findById(dto.getCategoryId())
	            .orElseThrow(() -> {
	                LOGGER.error("Category not found with ID: {}", dto.getCategoryId());
	                return new CategoryNotFoundException("Category not found with ID: " + dto.getCategoryId());
	            });
	        subCategory.setCategory(category);
	    }

	    try {
	        return subCategoryRepository.save(subCategory);
	    } catch (DataIntegrityViolationException ex) {
	        // This means DB unique constraint violated despite pre-check
	        LOGGER.error("Database constraint violation when updating SubCategory with ID {}: {}", id, ex.getMessage());
	        throw new DuplicateSubCategoryException("SubCategory with this name already exists in the selected category.");
	    }
	}

}
