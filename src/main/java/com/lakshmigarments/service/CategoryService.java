package com.lakshmigarments.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.CategoryRequestDTO;
import com.lakshmigarments.dto.CategoryResponseDTO;

@Service
public interface CategoryService {

    CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO);

    CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO);

    List<CategoryResponseDTO> getAllCategories(String search);

}
