package com.lakshmigarments.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.CategorySubCategoryCountDTO;

@Service
public interface MaterialLedgerService {
	
	List<CategorySubCategoryCountDTO> getCategorySubCategoryCounts();

}
