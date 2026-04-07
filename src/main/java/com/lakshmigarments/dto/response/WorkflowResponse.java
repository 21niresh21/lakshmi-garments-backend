package com.lakshmigarments.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.lakshmigarments.model.WorkflowRequestStatus;
import com.lakshmigarments.model.WorkflowRequestType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowResponse {
	private Long id;
	private WorkflowRequestType requestType;
	private WorkflowRequestStatus requestStatus;
	private String requestedBy;
	private LocalDateTime requestedAt;
	private String systemComments;
	private JsonNode payload;
}
