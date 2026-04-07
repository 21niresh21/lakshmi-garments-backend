package com.lakshmigarments.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class JobworkResponse {
	
	private Long id;
	private String jobworkNumber;
	private String jobworkType;
	private String jobworkOrigin;
	private String jobworkStatus;
	private String assignedTo;
	private String remarks;
	private String batchSerialCode;

}
