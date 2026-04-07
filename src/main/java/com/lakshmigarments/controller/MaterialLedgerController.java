package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.CategorySubCategoryCountDTO;
import com.lakshmigarments.service.MaterialLedgerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/material-inventory")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MaterialLedgerController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MaterialLedgerController.class);
    private final MaterialLedgerService ledgerService;
	
	@GetMapping
	public List<CategorySubCategoryCountDTO> getCategoryCount() {
		LOGGER.info("Received request to fetch category and subcategory counts.");
		List<CategorySubCategoryCountDTO> result = ledgerService.getCategorySubCategoryCounts();
		LOGGER.debug("Fetched {} category count records.", result.size());
		return result;
	}

}
