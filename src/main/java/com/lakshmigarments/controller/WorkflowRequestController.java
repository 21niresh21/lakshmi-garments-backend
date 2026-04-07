package com.lakshmigarments.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.BatchRequestDTO;
import com.lakshmigarments.dto.request.WorkflowRequestDTO;
import com.lakshmigarments.dto.response.WorkflowResponse;
import com.lakshmigarments.model.WorkflowRequest;
import com.lakshmigarments.model.WorkflowRequestStatus;
import com.lakshmigarments.model.WorkflowRequestType;
import com.lakshmigarments.service.ExcelFileGeneratorService;
import com.lakshmigarments.service.WorkflowRequestService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/workflow-requests")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@AllArgsConstructor
public class WorkflowRequestController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRequestController.class);
	private final WorkflowRequestService requestService;
	
	@PostMapping
	public ResponseEntity<WorkflowRequest> createRequest(@RequestBody @Valid WorkflowRequestDTO workflowRequestDTO) {
		LOGGER.info("Received request to create a new workflow request: {}", workflowRequestDTO);
		requestService.createWorkflowRequest(workflowRequestDTO);
		LOGGER.info("Workflow request created successfully");
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
	
	@GetMapping
	public Page<WorkflowResponse> getWorkflowRequests(
			@RequestParam(required = false) Integer pageNo,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false, defaultValue = "createdAt") String sortBy,
			@RequestParam(required = false, defaultValue = "desc") String sortDir,
			@RequestParam(required = false) List<String> requestedByNames,
			@RequestParam(required = false) List<WorkflowRequestType> requestTypes,
			@RequestParam(required = false) List<WorkflowRequestStatus> statuses,
			@RequestParam(required = false) LocalDate startDate,
			@RequestParam(required = false) LocalDate endDate) {
		return requestService.getAllWorkflowRequests(pageNo, pageSize, sortBy, sortDir, requestedByNames, requestTypes,
				statuses, startDate, endDate);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<WorkflowRequest> updateWorkflowRequest(
			@PathVariable Long id, @RequestBody WorkflowRequestDTO workflowRequestDTO){
		WorkflowRequest workflowRequest = requestService.updateWorkflowRequest(id, workflowRequestDTO);
		return new ResponseEntity<>(workflowRequest, HttpStatus.ACCEPTED);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<WorkflowResponse> getWorkflowRequest(
			@PathVariable Long id){
		WorkflowResponse responseDTO = requestService.getWorkflowRequest(id);
		return new ResponseEntity<>(responseDTO, HttpStatus.ACCEPTED);
	}
	

}
