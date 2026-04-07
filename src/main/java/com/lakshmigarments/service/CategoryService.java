package com.lakshmigarments.service;

import java.util.List;
import com.lakshmigarments.dto.request.CategoryRequest;
import com.lakshmigarments.dto.response.CategoryResponse;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest categoryRequest);

    CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest);

    List<CategoryResponse> getAllCategories(String search);

}
