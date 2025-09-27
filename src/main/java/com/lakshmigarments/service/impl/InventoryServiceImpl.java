package com.lakshmigarments.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.CategorySubCategoryCountDTO;
import com.lakshmigarments.dto.SubCategoryCountDTO;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.model.Inventory;
import com.lakshmigarments.service.InventoryService;
import com.lakshmigarments.repository.InventoryRepository;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);
    private final InventoryRepository inventoryRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
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
    public Long getCategorySubCategoryCount(String category, String subCategory) {
        Inventory inventory = inventoryRepository.findBySubCategoryNameAndCategoryName(subCategory, category).orElse(null);
        if (inventory == null) {
            LOGGER.error("Inventory not found with subCategory {} and category {}", subCategory, category);
            return 0L;
        }
        return inventory.getCount().longValue();
    }
}
