package com.lakshmigarments.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lakshmigarments.service.BaleService;

import jakarta.transaction.Transactional;

import com.lakshmigarments.exception.BaleNotFoundException;
import com.lakshmigarments.exception.CategoryNotFoundException;
import com.lakshmigarments.exception.SubCategoryNotFoundException;
import com.lakshmigarments.model.Bale;
import com.lakshmigarments.model.Category;
import com.lakshmigarments.model.SubCategory;
import com.lakshmigarments.dto.BaleDTO;
import com.lakshmigarments.repository.BaleRepository;
import com.lakshmigarments.repository.CategoryRepository;
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

    @Override
    @Transactional
    public void updateBale(Long baleId, BaleDTO baleDTO) {
        LOGGER.info("Updating bale with id: {}", baleId);
        Bale bale = baleRepository.findById(baleId).orElseThrow(() -> {
            LOGGER.error("Bale not found with id: {}", baleId);
            return new BaleNotFoundException("Bale not found with id: " + baleId);
        });
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
            Category category = categoryRepository.findByName(baleDTO.getCategory()).orElseThrow(() -> {
                LOGGER.error("Category not found with name: {}", baleDTO.getCategory());
                return new CategoryNotFoundException("Category not found with name: " + baleDTO.getCategory());
            });
            bale.setCategory(category);
        }
        if (baleDTO.getSubCategory() != null) {
            SubCategory subCategory = subCategoryRepository.findByName(baleDTO.getSubCategory()).orElseThrow(() -> {
                LOGGER.error("Sub category not found with name: {}", baleDTO.getSubCategory());
                return new SubCategoryNotFoundException("Sub category not found with name: " + baleDTO.getSubCategory());
            });
            bale.setSubCategory(subCategory);
        }
        baleRepository.save(bale);
        LOGGER.info("Bale updated successfully with id: {}", baleId);
    }
}
