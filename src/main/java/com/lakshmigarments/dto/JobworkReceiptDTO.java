package com.lakshmigarments.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobworkReceiptDTO {
	
	private String jobworkNumber;
	private Long receivedById;
	private List<JobworkReceiptItemDTO> jobworkReceiptItems;

}
