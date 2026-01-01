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
import com.lakshmigarments.model.Category;
import com.lakshmigarments.repository.BatchItemRepository;
import com.lakshmigarments.repository.MaterialLedgerRepository;
import com.lakshmigarments.service.MaterialLedgerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaterialLedgerServiceImpl implements MaterialLedgerService{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MaterialLedgerServiceImpl.class);
    private final MaterialLedgerRepository ledgerRepository;
    private final ModelMapper modelMapper;

	@Override
	public List<CategorySubCategoryCountDTO> getCategorySubCategoryCounts() {
		LOGGER.info("Fetching category and subcategory counts from inventory.");

        List<Object[]> result = ledgerRepository.getCategorySubCategoryCountWithPercentage();
        LOGGER.debug("Raw query result size: {}", result.size());

        // Use category name+code as key to avoid entity issues
        Map<String, CategorySubCategoryCountDTO> categoryMap = new HashMap<>();

        for (Object[] row : result) {
            Category category = (Category) row[0];
            String subCategoryName = (String) row[1];
            Long totalCount = (Long) row[2];
            Double percentage = (Double) row[3];

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
            if (totalCount != 0) {
                categoryDTO.getSubCategories().add(new SubCategoryCountDTO(subCategoryName, totalCount, 
                		percentage));
			}
        }

        List<CategorySubCategoryCountDTO> finalList = categoryMap.values().stream()
                .filter(dto -> !dto.getSubCategories().isEmpty())
                .collect(Collectors.toList());

        LOGGER.info("Returning {} category DTOs", finalList.size());
        return finalList;
	}

}
