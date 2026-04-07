package com.lakshmigarments.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.lakshmigarments.dto.CategorySubCategoryCountDTO;
import com.lakshmigarments.dto.SubCategoryCountDTO;
import com.lakshmigarments.dto.response.SubCategoryResponse;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.repository.CategoryRepository;
import com.lakshmigarments.repository.MaterialLedgerRepository;
import com.lakshmigarments.repository.SubCategoryRepository;
import com.lakshmigarments.service.InventoryService;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final MaterialLedgerRepository ledgerRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    public InventoryServiceImpl(MaterialLedgerRepository ledgerRepository,
                                 CategoryRepository categoryRepository,
                                 SubCategoryRepository subCategoryRepository) {
        this.ledgerRepository = ledgerRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    @Override
    public List<CategorySubCategoryCountDTO> getCategorySubCategoryCounts() {
    	LOGGER.debug("Fetching all inventory counts grouped by category and subcategory.");

        // fetch all rows from ledger grouped by category
        List<Object[]> rows = ledgerRepository.getStockGroupedByCategory();

        // group rows by category
        Map<String, List<SubCategoryCountDTO>> grouped = new LinkedHashMap<>();
        Map<String, String> categoryCodeMap = new LinkedHashMap<>();
        Map<String, Long> categoryTotalMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String categoryName = (String) row[0];
            String categoryCode = (String) row[1];
            String subCategoryName = (String) row[2];
            Long count = ((Number) row[3]).longValue();

            categoryCodeMap.put(categoryName, categoryCode);
            categoryTotalMap.merge(categoryName, count, Long::sum);
            grouped.computeIfAbsent(categoryName, k -> new ArrayList<>())
                   .add(new SubCategoryCountDTO(subCategoryName, count, null)); // percentage filled below
        }

        return grouped.entrySet().stream().map(entry -> {
            String categoryName = entry.getKey();
            long total = categoryTotalMap.get(categoryName);

            // Skip this category if total quantity is 0
            if (total == 0) {
                return null;
            }

            List<SubCategoryCountDTO> subCategoryDTOs = entry.getValue().stream().map(sc ->
                new SubCategoryCountDTO(
                    sc.getSubCategoryName(),
                    sc.getCount(),
                    total > 0 ? (sc.getCount() * 100.0) / total : 0.0
                )
            ).collect(Collectors.toList());

            return new CategorySubCategoryCountDTO(
                categoryName,
                categoryCodeMap.get(categoryName),
                subCategoryDTOs
            );
        }).filter(dto -> dto != null) // Filter out null categories (those with 0 total quantity)
          .collect(Collectors.toList());
    }

	@Override
	public Long getCategorySubCategoryCount(Long categoryId, Long subCategoryId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SubCategoryResponse> getSubCategories(Long categoryId) {
		// TODO Auto-generated method stub
		return null;
	}

//    @Override
//    public Long getCategorySubCategoryCount(Long categoryId, Long subCategoryId) {
//        LOGGER.debug("Fetching stock count for categoryId: {}, subCategoryId: {}", categoryId, subCategoryId);
//        return ledgerRepository.getStockCount(categoryId, subCategoryId);
//    }
//
//    @Override
//    public List<SubCategoryResponse> getSubCategories(Long categoryId) {
//        LOGGER.debug("Fetching subcategories for categoryId: {}", categoryId);
//
//        return subCategoryRepository.findByCategoryId(categoryId)
//            .stream()
//            .map(sc -> {
//                SubCategoryResponse response = new SubCategoryResponse();
//                response.setId(sc.getId());
//                response.setName(sc.getName());
//                return response;
//            })
//            .collect(Collectors.toList());
//    }
}