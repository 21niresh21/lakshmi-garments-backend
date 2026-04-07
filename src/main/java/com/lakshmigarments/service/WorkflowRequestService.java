package com.lakshmigarments.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.request.WorkflowRequestDTO;
import com.lakshmigarments.dto.response.WorkflowResponse;
import com.lakshmigarments.model.WorkflowRequest;
import com.lakshmigarments.model.WorkflowRequestStatus;
import com.lakshmigarments.model.WorkflowRequestType;

@Service
public interface WorkflowRequestService {

	WorkflowRequest createWorkflowRequest(WorkflowRequestDTO workflowRequestDTO);

	Page<WorkflowResponse> getAllWorkflowRequests(Integer pageNo, Integer pageSize, String sortBy, String sortDir,
			List<String> requestedByNames, List<WorkflowRequestType> requestTypes, List<WorkflowRequestStatus> statuses,
			LocalDate startDate, LocalDate endDate);

	WorkflowRequest updateWorkflowRequest(Long id, WorkflowRequestDTO workflowRequestDTO);

	WorkflowResponse getWorkflowRequest(Long id);

}

