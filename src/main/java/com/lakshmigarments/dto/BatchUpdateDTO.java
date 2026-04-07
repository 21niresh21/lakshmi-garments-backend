package com.lakshmigarments.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchUpdateDTO {   
        
    private String serialCode;
    
    private String categoryName;
    
    private String batchStatusName;
    
    private Boolean isUrgent;
    
    private String remarks;
    
    private List<BatchSubCategoryRequestDTO> subCategories;
}