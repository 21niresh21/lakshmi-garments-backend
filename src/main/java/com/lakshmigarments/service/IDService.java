package com.lakshmigarments.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lakshmigarments.repository.BatchRepository;
import com.lakshmigarments.repository.CategoryRepository;
import com.lakshmigarments.repository.LorryReceiptRepository;

@Service
public class IDService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IDService.class);
	private final LorryReceiptRepository lorryReceiptRepository;
	private final BatchRepository batchRepository;
	private final CategoryRepository categoryRepository;
	
	public IDService(LorryReceiptRepository lorryReceiptRepository, BatchRepository batchRepository,
			CategoryRepository categoryRepository) {
		this.lorryReceiptRepository = lorryReceiptRepository;
		this.batchRepository = batchRepository;
		this.categoryRepository =categoryRepository;
	}
	
	public Long getNextLRID() {
		Long lrCount = lorryReceiptRepository.count();
		return lrCount + 1;
	}
	
	public String getSerialCode(String categoryName) {
	    String categoryCode = categoryRepository.findCodeByName(categoryName).orElse(null);
	    
	    // Get current financial year (e.g., "25/26" for April 2025 - March 2026)
	    String financialYear = getCurrentFinancialYear();
	    
	    // Build the prefix: CategoryCode + FinancialYear + "-"
	    String prefix = categoryCode + financialYear + "-";
	    
	    // Get all serial codes for this category, ordered by createdAt DESC
	    List<String> allSerialCodes = batchRepository.findAllSerialCodesByCategoryName(categoryName);
	    
	    // Find the latest serial code that matches the current financial year
	    String latestInFinancialYear = null;
	    for (String serialCode : allSerialCodes) {
	        if (serialCode != null && serialCode.contains(prefix)) {
	            latestInFinancialYear = serialCode;
	            break; // Since list is ordered by createdAt DESC, first match is the latest
	        }
	    }
	    
	    if (latestInFinancialYear == null) {
	        // No batches in this financial year yet, start with 0001
	        return prefix + "0001";
	    }

	    // Extract the numeric part from the latest serial code
	    // Format expected: P25/26-0001
	    // Find the position of the last hyphen and extract everything after it
	    int lastHyphenIndex = latestInFinancialYear.lastIndexOf('-');
	    if (lastHyphenIndex == -1 || lastHyphenIndex >= latestInFinancialYear.length() - 1) {
	        // If no hyphen found or nothing after hyphen, start fresh
	        return prefix + "0001";
	    }
	    
	    String numericPartStr = latestInFinancialYear.substring(lastHyphenIndex + 1);
	    
	    // Strip any parenthetical suffix like (U) from numeric part
	    if (numericPartStr.contains("(")) {
	        numericPartStr = numericPartStr.substring(0, numericPartStr.indexOf('(')).trim();
	    }
	    
	    try {
	        Integer numericPart = Integer.parseInt(numericPartStr.trim());
	        numericPart++;
	        String incrementedNumericPart = String.format("%04d", numericPart);
	        return prefix + incrementedNumericPart;
	    } catch (NumberFormatException e) {
	        LOGGER.error("Failed to parse numeric part from serial code: {}", latestInFinancialYear, e);
	        // If parsing fails, start fresh with 0001
	        return prefix + "0001";
	    }
	}
	
	/**
	 * Gets the current financial year in format "YY/YY"
	 * Financial year runs from April 1st to March 31st of next year
	 * Example: April 2025 - March 2026 returns "25/26"
	 */
	private String getCurrentFinancialYear() {
	    LocalDate now = LocalDate.now();
	    int year = now.getYear();
	    int month = now.getMonthValue();
	    
	    // Financial year starts in April
	    int startYear, endYear;
	    if (month >= 4) {
	        // We're in April or later, so financial year is current year to next year
	        startYear = year % 100;  // Get last 2 digits
	        endYear = (year + 1) % 100;
	    } else {
	        // We're before April, so financial year is previous year to current year
	        startYear = (year - 1) % 100;
	        endYear = year % 100;
	    }
	    
	    // Format as "YY/YY" with leading zeros if needed
	    return String.format("%02d/%02d", startYear, endYear);
	}


}
