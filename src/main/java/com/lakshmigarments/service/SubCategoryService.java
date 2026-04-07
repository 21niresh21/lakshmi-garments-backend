package com.lakshmigarments.service;

import java.util.List;
import com.lakshmigarments.dto.request.SubCategoryRequest;
import com.lakshmigarments.dto.response.SubCategoryResponse;

public interface SubCategoryService {

    SubCategoryResponse createSubCategory(SubCategoryRequest subCategoryRequest);
    
    SubCategoryResponse updateSubCategory(Long id, SubCategoryRequest subCategoryRequest);
    
    List<SubCategoryResponse> getAllSubCategories(String search);

}
