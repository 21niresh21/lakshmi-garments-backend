package com.lakshmigarments.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchItemResponse {

    Long id;

    String name;
    
    Long availableQuantity;
}
