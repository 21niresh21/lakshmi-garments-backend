package com.lakshmigarments.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.CategorySubCategoryCountDTO;
import com.lakshmigarments.dto.SubCategoryCountDTO;
import com.lakshmigarments.dto.SubCategoryResponseDTO;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.model.Inventory;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.service.InventoryService;
import com.lakshmigarments.repository.InventoryRepository;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    public InventoryServiceImpl(InventoryRepository inventoryRepository, ModelMapper modelMapper) {
        this.inventoryRepository = inventoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<CategorySubCategoryCountDTO> getCategorySubCategoryCounts() {
        LOGGER.info("Fetching category and subcategory counts from inventory.");

        List<Object[]> result = inventoryRepository.getCategorySubCategoryCount();
        LOGGER.debug("Raw query result size: {}", result.size());

        // Use category name+code as key to avoid entity issues
        Map<String, CategorySubCategoryCountDTO> categoryMap = new HashMap<>();

        for (Object[] row : result) {
            Category category = (Category) row[0];
            String subCategoryName = (String) row[1];
            Long totalCount = (Long) row[2];

            String categoryKey = category.getName();

            CategorySubCategoryCountDTO categoryDTO = categoryMap.computeIfAbsent(
                    categoryKey,
                    k -> {
                        LOGGER.debug("Creating new DTO for category: {} ({})", category.getName(), category.getCode());
                        return new CategorySubCategoryCountDTO(
                                category.getName(),
                                category.getCode(),
                                new ArrayList<>());
                    });

            LOGGER.debug("Adding subcategory '{}' with count {} to category '{}'", subCategoryName, totalCount,
                    category.getName());
            categoryDTO.getSubCategories().add(new SubCategoryCountDTO(subCategoryName, totalCount));
        }

        List<CategorySubCategoryCountDTO> finalList = new ArrayList<>(categoryMap.values());
        LOGGER.info("Returning {} category DTOs", finalList.size());
        return finalList;
    }

    @Override
    public Long getCategorySubCategoryCount(Long categoryId, Long subCategoryId) {
        Inventory inventory = inventoryRepository.findByCategoryIdAndSubCategoryId(categoryId, subCategoryId).orElse(null);
        if (inventory == null) {
            LOGGER.error("Inventory not found with subCategory {} and category {}", subCategoryId, categoryId);
            return 0L;
        }
        return inventory.getCount().longValue();
    }

    @Override
    public List<SubCategoryResponseDTO> getSubCategories(Long categoryId) {
        List<SubCategory> subCategories = inventoryRepository.findSubCategoriesByCategoryId(categoryId);
        return subCategories.stream().map(subCategory -> modelMapper.map(subCategory, SubCategoryResponseDTO.class)).collect(Collectors.toList());
    }
}
