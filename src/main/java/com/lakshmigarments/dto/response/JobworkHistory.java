package com.lakshmigarments.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobworkHistory {

	private String jobworkNumber;
	private String jobworkOrigin;
	private String jobworkType;
	private String batchSerialCode;
	private String assignedTo;
	private String assignedBy;
	private String jobworkStatus;
	
}
