package com.lakshmigarments.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.CategoryRequestDTO;
import com.lakshmigarments.dto.CategoryResponseDTO;
import com.lakshmigarments.exception.CategoryNotFoundException;
import com.lakshmigarments.exception.DuplicateCategoryException;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.repository.CategoryRepository;
import com.lakshmigarments.repository.specification.CategorySpecification;
import com.lakshmigarments.service.CategoryService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) {

        String categoryName = categoryRequestDTO.getName().trim();
        String categoryCode = categoryRequestDTO.getCode().trim();

        if (categoryRepository.existsByNameIgnoreCase(categoryName)
                || categoryRepository.existsByCodeIgnoreCase(categoryCode)) {
            LOGGER.error("Category already exists with name {} or code {}", categoryName, categoryCode);
            throw new DuplicateCategoryException(
                    "Category already exists with name " + categoryName + " or code " + categoryCode);
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setCode(categoryCode);

        Category savedCategory = categoryRepository.save(category);
        LOGGER.debug("Category created with name {}", savedCategory.getName());
        return modelMapper.map(savedCategory, CategoryResponseDTO.class);
    }

    @Override
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO) {

        String categoryName = categoryRequestDTO.getName().trim();
        String categoryCode = categoryRequestDTO.getCode().trim();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.error("Category not found with ID: {}", id);
                    return new CategoryNotFoundException("Category not found with ID: " + id);
                });

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(categoryName, id)
                || categoryRepository.existsByCodeIgnoreCaseAndIdNot(categoryCode, id)) {
            LOGGER.error("Category already exists with name {} or code {}", categoryName, categoryCode);
            throw new DuplicateCategoryException(
                    "Category already exists with name " + categoryName + " or code " + categoryCode);
        }

        category.setName(categoryName);
        category.setCode(categoryCode);
        Category updatedCategory = categoryRepository.save(category);
        LOGGER.debug("Category updated with name {}", updatedCategory.getName());
        return modelMapper.map(updatedCategory, CategoryResponseDTO.class);
    }

    @Override
    public List<CategoryResponseDTO> getAllCategories(String search) {
        Specification<Category> spec = CategorySpecification.filterByName(search);
        List<Category> categories = categoryRepository.findAll(spec);
        LOGGER.debug("Found {} category(s) matching filter", categories.size());
        return categories.stream().map(category -> modelMapper.map(category, CategoryResponseDTO.class))
                .collect(Collectors.toList());
    }
}
