package com.lakshmigarments.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobworkRequestDTO {
	
	private Long employeeId;
	private Long batchId;
	private Long itemId;
	private Long quantity;
	private Long jobworkTypeId;
	private String jobworkNumber;

}
