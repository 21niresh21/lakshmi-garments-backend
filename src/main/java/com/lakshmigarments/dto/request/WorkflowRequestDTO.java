package com.lakshmigarments.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRequestDTO {

	private String requestType;
	private String requestStatus;
	private String payload;
	private String remarks;
	private String systemComments;
	
}
