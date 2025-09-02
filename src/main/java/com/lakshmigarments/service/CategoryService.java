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

import com.lakshmigarments.dto.CreateCategoryDTO;
import com.lakshmigarments.exception.DuplicateCategoryException;
import com.lakshmigarments.exception.DuplicateSubCategoryException;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.repository.CategoryRepository;

@Service
public class CategoryService {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryService.class);
	private final CategoryRepository categoryRepository;
	private final ModelMapper modelMapper;
	
	public CategoryService(CategoryRepository categoryRepository, ModelMapper modelMapper) {
		this.categoryRepository = categoryRepository;
		this.modelMapper = modelMapper;
	}
	
	public Category createCategory(CreateCategoryDTO createCategoryDTO) {
		
		if (categoryRepository.existsByName(createCategoryDTO.getName())) {
    		LOGGER.error("Failed to create category: Name already exists");
	        throw new DuplicateCategoryException("Category with the same name already exists");
		} 
		
		if (categoryRepository.existsByCode(createCategoryDTO.getCode())) {
			LOGGER.error("Failed to create category: Code already exists");
	        throw new DuplicateSubCategoryException("Category Code with the same name already exists");
		}
		
		Category category = modelMapper.map(createCategoryDTO, Category.class);
        Category createdCategory = categoryRepository.save(category);
        LOGGER.info("Created category with name {}", createdCategory.getName());
        return createdCategory;
	  
	}

	
	public Page<Category> getCategories(Integer pageNo, Integer pageSize, String sortBy, String sortDir) {
		
		if (pageSize == null) {
			LOGGER.info("Retrieved all categories");
			Pageable wholePage = Pageable.unpaged();
			return categoryRepository.findAll(wholePage);
		}
		
		Sort sort  = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
						? Sort.by(sortBy).ascending()
						: Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
		Page<Category> categoryPage = categoryRepository.findAll(pageable);
		
		LOGGER.info("Retrieved categories as pages");
		return categoryPage;
	}
}
