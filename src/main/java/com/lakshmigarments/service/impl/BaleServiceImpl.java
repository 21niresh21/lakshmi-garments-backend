package com.lakshmigarments.service.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lakshmigarments.service.BaleService;

import jakarta.transaction.Transactional;

import com.lakshmigarments.exception.BaleNotFoundException;
import com.lakshmigarments.exception.CategoryNotFoundException;
import com.lakshmigarments.exception.SubCategoryNotFoundException;
import com.lakshmigarments.model.Bale;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.model.Inventory;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.dto.BaleDTO;
import com.lakshmigarments.repository.BaleRepository;
import com.lakshmigarments.repository.CategoryRepository;
import com.lakshmigarments.repository.InventoryRepository;
import com.lakshmigarments.repository.SubCategoryRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BaleServiceImpl implements BaleService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaleServiceImpl.class);

	private final BaleRepository baleRepository;
	private final CategoryRepository categoryRepository;
	private final SubCategoryRepository subCategoryRepository;
	private final InventoryRepository inventoryRepository;

	@Override
	 @Transactional
    public void updateBale(Long baleId, BaleDTO baleDTO) {

        LOGGER.info("Updating bale with id: {}", baleId);

        Bale bale = baleRepository.findById(baleId)
                .orElseThrow(() -> new BaleNotFoundException(
                        "Bale not found with id: " + baleId));

        /* ----------------------------
           1️⃣ Capture OLD state
        ----------------------------- */
        Long oldQty = bale.getQuantity();
        Category oldCategory = bale.getCategory();
        SubCategory oldSubCategory = bale.getSubCategory();

        /* ----------------------------
           2️⃣ Apply NEW values
        ----------------------------- */

        if (baleDTO.getBaleNumber() != null) {
            bale.setBaleNumber(baleDTO.getBaleNumber());
        }

        if (baleDTO.getQuantity() != null) {
            bale.setQuantity(baleDTO.getQuantity());
        }

        if (baleDTO.getLength() != null) {
            bale.setLength(baleDTO.getLength());
        }

        if (baleDTO.getPrice() != null) {
            bale.setPrice(baleDTO.getPrice());
        }

        if (baleDTO.getQuality() != null) {
            bale.setQuality(baleDTO.getQuality());
        }

        if (baleDTO.getCategory() != null) {
            Category category = categoryRepository.findByName(baleDTO.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException(
                            "Category not found: " + baleDTO.getCategory()));
            bale.setCategory(category);
        }

        if (baleDTO.getSubCategory() != null) {
            SubCategory subCategory = subCategoryRepository.findByName(baleDTO.getSubCategory())
                    .orElseThrow(() -> new SubCategoryNotFoundException(
                            "SubCategory not found: " + baleDTO.getSubCategory()));
            bale.setSubCategory(subCategory);
        }

        /* ----------------------------
           3️⃣ Capture NEW state
        ----------------------------- */
        Long newQty = bale.getQuantity();
        Category newCategory = bale.getCategory();
        SubCategory newSubCategory = bale.getSubCategory();

        /* ----------------------------
           4️⃣ Adjust inventory
        ----------------------------- */
        boolean inventoryChanged =
                !Objects.equals(oldQty, newQty) ||
                !oldCategory.getId().equals(newCategory.getId()) ||
                !oldSubCategory.getId().equals(newSubCategory.getId());

        if (inventoryChanged) {
        	// only quantity changes
        	if (!Objects.equals(oldQty, newQty) && oldCategory.getId().equals(newCategory.getId()) && oldSubCategory.getId().equals(newSubCategory.getId())) {
        		adjustInventory(newCategory, newSubCategory, newQty-oldQty);
			} else {
				adjustInventory(oldCategory, oldSubCategory, -oldQty);
				adjustInventory(newCategory, newSubCategory, newQty);
			}
//            // Revert OLD inventory
//            adjustInventory(oldCategory, oldSubCategory, oldQty);
//
//            // Apply NEW inventory
//            adjustInventory(newCategory, newSubCategory, -newQty);
        }

        baleRepository.save(bale);

        LOGGER.info("Bale updated successfully with id: {}", baleId);
    }
	
	private void adjustInventory(Category category, SubCategory subCategory, Long qtyChange) {

        Inventory inventory = inventoryRepository
                .findByCategoryIdAndSubCategoryId(category.getId(), subCategory.getId())
                .orElseGet(() -> {
                    Inventory inv = new Inventory();
                    inv.setCategory(category);
                    inv.setSubCategory(subCategory);
                    inv.setCount(0L);
                    return inv;
                });

        long newCount = inventory.getCount() + qtyChange;

        if (newCount < 0) {
            throw new IllegalStateException(
                "Insufficient inventory for " +
                category.getName() + " / " + subCategory.getName()
            );
        }

        inventory.setCount(newCount);
        inventoryRepository.save(inventory);
    }
}
