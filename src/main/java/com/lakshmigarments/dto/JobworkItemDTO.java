package com.lakshmigarments.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobworkItemDTO {
	
	private Long id;
	private String itemName;
	private String jobworkNumber;
	private Long quantity;
	private String status;

}
