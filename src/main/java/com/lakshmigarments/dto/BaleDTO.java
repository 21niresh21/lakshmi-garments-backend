package com.lakshmigarments.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BaleDTO {

	private Long id;
	
	private String baleNumber;
	
	private Long quantity;
	
	private Double length;
	
	private Double price;
	
	private String quality;
		
	private String category;
	
	private String subCategory;
	
}
