package com.lakshmigarments.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobworkItemResponse {
	
	private String itemName;
	private Long acceptedQuantity = 0L;
	private Long salesQuantity = 0L;
	private Double salesPrice = 0.0;
	private Double wagePerItem = 0.0;
	private Long damagedQuantity = 0L;
	
}
