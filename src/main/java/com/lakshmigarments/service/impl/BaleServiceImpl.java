package com.lakshmigarments.service.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lakshmigarments.service.BaleService;
import com.lakshmigarments.service.policy.EditWindowPolicy;

import jakarta.transaction.Transactional;

import com.lakshmigarments.exception.BaleNotFoundException;
import com.lakshmigarments.exception.CategoryNotFoundException;
import com.lakshmigarments.exception.SubCategoryNotFoundException;
import com.lakshmigarments.exception.UserNotFoundException;
import com.lakshmigarments.model.Bale;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.model.Inventory;
import com.lakshmigarments.model.Invoice;
import com.lakshmigarments.model.LedgerDirection;
import com.lakshmigarments.model.MaterialInventoryLedger;
import com.lakshmigarments.model.MovementType;
import com.lakshmigarments.model.ReferenceType;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.model.User;
import com.lakshmigarments.dto.BaleDTO;
import com.lakshmigarments.repository.BaleRepository;
import com.lakshmigarments.repository.CategoryRepository;
import com.lakshmigarments.repository.InventoryRepository;
import com.lakshmigarments.repository.MaterialLedgerRepository;
import com.lakshmigarments.repository.SubCategoryRepository;
import com.lakshmigarments.repository.UserRepository;

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
	private final EditWindowPolicy editWindowPolicy;
	private final UserRepository userRepository;
	private final MaterialLedgerRepository ledgerRepository;

	@Override
	@Transactional
	public void updateBale(Long baleId, BaleDTO baleDTO) {

		LOGGER.info("Updating bale with id: {}", baleId);

		Bale bale = baleRepository.findById(baleId)
				.orElseThrow(() -> new BaleNotFoundException("Bale not found with id: " + baleId));

		User user = userRepository.findById(baleDTO.getUpdatedById()).orElseThrow(() -> {
			LOGGER.error("User with ID {} not found", baleDTO.getUpdatedById());
			return new UserNotFoundException("User not found with ID " + baleDTO.getUpdatedById());
		});

		Invoice invoice = bale.getLorryReceipt().getInvoice();
		editWindowPolicy.validateEditPermission(invoice.getCreatedAt(), user.getRole().getName());

		/*
		 * ---------------------------- 1️⃣ Capture OLD state
		 * -----------------------------
		 */
		Long oldQty = bale.getQuantity();
		Category oldCategory = bale.getCategory();
		SubCategory oldSubCategory = bale.getSubCategory();

		/*
		 * ---------------------------- 2️⃣ Apply NEW values
		 * -----------------------------
		 */

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
					.orElseThrow(() -> new CategoryNotFoundException("Category not found: " + baleDTO.getCategory()));
			bale.setCategory(category);
		}

		if (baleDTO.getSubCategory() != null) {
			SubCategory subCategory = subCategoryRepository.findByName(baleDTO.getSubCategory()).orElseThrow(
					() -> new SubCategoryNotFoundException("SubCategory not found: " + baleDTO.getSubCategory()));
			bale.setSubCategory(subCategory);
		}

		/*
		 * ---------------------------- 3️⃣ Capture NEW state
		 * -----------------------------
		 */
		Long newQty = bale.getQuantity();
		Category newCategory = bale.getCategory();
		SubCategory newSubCategory = bale.getSubCategory();
		
		MaterialInventoryLedger inventory;
		inventory = new MaterialInventoryLedger();
//		inventory.setDirection(LedgerDirection.IN);
    	inventory.setMovementType(MovementType.BALE_EDIT);
    	inventory.setReferenceType(ReferenceType.BALE);
    	inventory.setReference_id(bale.getId());
    	inventory.setUnit("piece(s)");
		
		inventory.setCreatedBy(user);
	
		

		/*
		 * ---------------------------- 4️⃣ Adjust inventory
		 * -----------------------------
		 */
		boolean inventoryChanged = !Objects.equals(oldQty, newQty) || !oldCategory.getId().equals(newCategory.getId())
				|| !oldSubCategory.getId().equals(newSubCategory.getId());

		if (inventoryChanged) {
			// only quantity changes
			if (!Objects.equals(oldQty, newQty) && oldCategory.getId().equals(newCategory.getId())
					&& oldSubCategory.getId().equals(newSubCategory.getId())) {
				adjustInventory(newCategory, newSubCategory, newQty - oldQty);
				inventory.setQuantity(Math.abs(newQty - oldQty));
				inventory.setSubCategory(newSubCategory);
				inventory.setCategory(newCategory);
				if (newQty >= oldQty) {
					inventory.setDirection(LedgerDirection.IN);
				} else {
					inventory.setDirection(LedgerDirection.OUT);
				}
				ledgerRepository.save(inventory);
			} else {
				adjustInventory(oldCategory, oldSubCategory, -oldQty);
				
				inventory.setQuantity(oldQty);
				inventory.setSubCategory(oldSubCategory);
				inventory.setCategory(oldCategory);
				inventory.setDirection(LedgerDirection.OUT);
				ledgerRepository.save(inventory);
				
				adjustInventory(newCategory, newSubCategory, newQty);
				
				MaterialInventoryLedger inventoryForNewCategory;
				inventoryForNewCategory = new MaterialInventoryLedger();
//				inventory.setDirection(LedgerDirection.IN);
		    	inventoryForNewCategory.setMovementType(MovementType.BALE_EDIT);
				inventoryForNewCategory.setReferenceType(ReferenceType.BALE);
				inventoryForNewCategory.setReference_id(bale.getId());
				inventoryForNewCategory.setUnit("piece(s)");
				inventoryForNewCategory.setQuantity(newQty);
				inventoryForNewCategory.setSubCategory(newSubCategory);
				inventoryForNewCategory.setCategory(newCategory);
				inventoryForNewCategory.setDirection(LedgerDirection.IN);
				inventoryForNewCategory.setCreatedBy(user);
				ledgerRepository.save(inventoryForNewCategory);
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
				.findByCategoryIdAndSubCategoryId(category.getId(), subCategory.getId()).orElseGet(() -> {
					Inventory inv = new Inventory();
					inv.setCategory(category);
					inv.setSubCategory(subCategory);
					inv.setCount(0L);
					return inv;
				});

		long newCount = inventory.getCount() + qtyChange;

		if (newCount < 0) {
			throw new IllegalStateException(
					"Insufficient inventory for " + category.getName() + " / " + subCategory.getName());
		}

		inventory.setCount(newCount);
		inventoryRepository.save(inventory);
	}
}
