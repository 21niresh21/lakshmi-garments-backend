package com.lakshmigarments.dto;

import java.util.List;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class CategorySubCategoryCountDTO {

    private String categoryName;
    private String categoryCode;
    private List<SubCategoryCountDTO> subCategories;

}
