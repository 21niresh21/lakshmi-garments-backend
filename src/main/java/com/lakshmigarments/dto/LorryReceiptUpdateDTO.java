package com.lakshmigarments.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LorryReceiptUpdateDTO {
    
	@NotBlank
    private String lrNumber;

}
