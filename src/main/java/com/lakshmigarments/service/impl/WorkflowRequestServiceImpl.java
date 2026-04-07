package com.lakshmigarments.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakshmigarments.configuration.ModelMapperConfig;
import com.lakshmigarments.configuration.ObjectMapperConfig;
import com.lakshmigarments.context.UserContext;
import com.lakshmigarments.context.UserInfo;
import com.lakshmigarments.controller.UserController;
import com.lakshmigarments.dto.request.CreateJobworkReceiptRequest;
import com.lakshmigarments.dto.request.WorkflowRequestDTO;
import com.lakshmigarments.dto.response.WorkflowResponse;
import com.lakshmigarments.exception.JobworkNotFoundException;
import com.lakshmigarments.exception.UserNotFoundException;
import com.lakshmigarments.exception.WorkflowRequestNotFoundException;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.model.JobworkStatus;
import com.lakshmigarments.model.User;
import com.lakshmigarments.model.WorkflowRequest;
import com.lakshmigarments.model.WorkflowRequestStatus;
import com.lakshmigarments.model.WorkflowRequestType;
import com.lakshmigarments.repository.JobworkRepository;
import com.lakshmigarments.repository.UserRepository;
import com.lakshmigarments.repository.WorkflowRequestRepository;
import com.lakshmigarments.repository.specification.WorfklowRequestSpecification;
import com.lakshmigarments.service.WorkflowRequestService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkflowRequestServiceImpl implements WorkflowRequestService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRequestServiceImpl.class);
	private final WorkflowRequestRepository requestRepository;
	private final ObjectMapper objectMapper;
	private final UserRepository userRepository;
	private final JobworkRepository jobworkRepository;
	private final ModelMapper modelMapper;

	@Override
	public WorkflowRequest createWorkflowRequest(WorkflowRequestDTO workflowRequestDTO) {

//		try {
//			JobworkReceiptDTO jobworkReceiptDTO = objectMapper.readValue(
//					workflowRequestDTO.getPayload(), JobworkReceiptDTO.class);
//			System.out.println(jobworkReceiptDTO.getJobworkReceiptItems().get(0).getItemName());
//		} catch (JsonMappingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		UserInfo userInfo = UserContext.get();
		User user = userRepository.findById(Long.valueOf(1)).orElse(null);
		
		if (workflowRequestDTO.getRequestType().equals(WorkflowRequestType.JOBWORK_RECEIPT.toString())) {
			try {
				JsonNode root = objectMapper.readTree(workflowRequestDTO.getPayload());
				String jobworkNumber = root.get("jobworkNumber").asText();
				Jobwork jobwork = jobworkRepository.findByJobworkNumber(jobworkNumber).orElse(null);
				if (jobwork == null) {
					throw new JobworkNotFoundException("No Jobwork found with ID " + jobworkNumber);
				}
				jobwork.setJobworkStatus(JobworkStatus.AWAITING_APPROVAL);
				jobworkRepository.save(jobwork);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		WorkflowRequest workflowRequest = new WorkflowRequest();
		workflowRequest.setPayload(workflowRequestDTO.getPayload());
		workflowRequest.setRemarks(workflowRequestDTO.getRemarks());
		workflowRequest.setSystemComments(workflowRequestDTO.getSystemComments());
		workflowRequest.setWorkflowRequestType(WorkflowRequestType.valueOf(workflowRequestDTO.getRequestType()));
		workflowRequest.setWorkflowRequestStatus(WorkflowRequestStatus.PENDING);
		return requestRepository.save(workflowRequest);
//		return null;
	}

	@Override
	public Page<WorkflowResponse> getAllWorkflowRequests(Integer pageNo, Integer pageSize, String sortBy, String sortDir,
			List<String> requestedByNames, List<WorkflowRequestType> requestTypes, List<WorkflowRequestStatus> statuses,
			LocalDate startDate, LocalDate endDate) {

		if (pageNo == null) {
			pageNo = 0;
		}
		if (pageSize == null || pageSize == 0) {
			pageSize = 10;
		}

		Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

		Specification<WorkflowRequest> specification = Specification
				.where(WorfklowRequestSpecification.filterByRequestedNames(requestedByNames))
				.and(WorfklowRequestSpecification.filterByRequestType(requestTypes))
				.and(WorfklowRequestSpecification.filterByStatus(statuses))
				.and(WorfklowRequestSpecification.filterByDateRange(startDate, endDate));
		Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

		Page<WorkflowRequest> workflowRequestPage = requestRepository.findAll(specification, pageable);

		return workflowRequestPage.map(this::convertToWorkflowResponseDTO);
	}
	
	private WorkflowResponse convertToWorkflowResponseDTO(WorkflowRequest entity) {
	    WorkflowResponse mappeResponseDTO = modelMapper.map(entity, WorkflowResponse.class);
	    mappeResponseDTO.setRequestType(entity.getWorkflowRequestType());
	    mappeResponseDTO.setRequestStatus(entity.getWorkflowRequestStatus());
	    mappeResponseDTO.setPayload(this.parseJson(entity.getPayload()));
	    mappeResponseDTO.setRequestedBy(entity.getCreatedBy());
	    mappeResponseDTO.setRequestedAt(entity.getCreatedAt());
	    return mappeResponseDTO;
	}

	@Override
	public WorkflowRequest updateWorkflowRequest(Long id, WorkflowRequestDTO workflowRequestDTO) {
		
//		UserInfo userInfo = UserContext.get();
//		Long userId = Long.valueOf(userInfo.getUserId());
//		User user = userRepository.findById(userId).orElseThrow(() -> {
//			LOGGER.error("User with ID {} not found", userId);
//			return new UserNotFoundException("User not found with ID " + userId);
//		});
		
		WorkflowRequest workflowRequest = requestRepository.findById(id).orElseThrow(() -> {
			LOGGER.error("Workflow Request with ID {} not found", id);
			return new WorkflowRequestNotFoundException("Workflow Request not found with ID " + id);
		});

		switch (workflowRequestDTO.getRequestStatus()) {
		case "APPROVED":
			workflowRequest.setWorkflowRequestStatus(WorkflowRequestStatus.APPROVED);
			break;
		case "REJECTED":
			// need to extract the payload and set batch jobwork status to IN_PROGRESS
			try {
				JsonNode root = objectMapper.readTree(workflowRequest.getPayload());
				String jobworkNumber = root.get("jobworkNumber").asText();
				Jobwork jobwork = jobworkRepository.findByJobworkNumber(jobworkNumber).orElse(null);
				if (jobwork != null) {
					jobwork.setJobworkStatus(JobworkStatus.IN_PROGRESS);
					jobworkRepository.save(jobwork);
				}
			} catch (JsonProcessingException e) {
				LOGGER.error("Error processing JSON payload for workflow request with ID {}", id);
				e.printStackTrace();
			}
			workflowRequest.setWorkflowRequestStatus(WorkflowRequestStatus.REJECTED);
			break;
		default:
			workflowRequest.setWorkflowRequestStatus(WorkflowRequestStatus.valueOf(workflowRequestDTO.getRequestStatus()));
			break;
		}
		
				
		return requestRepository.save(workflowRequest);
	}

	@Override
	public WorkflowResponse getWorkflowRequest(Long id) {
		
		WorkflowRequest workflowRequest = requestRepository.findById(id).orElseThrow(() -> {
			LOGGER.error("Workflow Request with ID {} not found", id);
			return new WorkflowRequestNotFoundException("Workflow Request not found with ID " + id);
		});
		
		WorkflowResponse dto = new WorkflowResponse();
		dto.setId(id);
		dto.setRequestedAt(workflowRequest.getCreatedAt());
		dto.setRequestedBy(workflowRequest.getCreatedBy());
		dto.setRequestStatus(workflowRequest.getWorkflowRequestStatus());
		dto.setRequestType(workflowRequest.getWorkflowRequestType());
		
		try {
            dto.setPayload(objectMapper.readTree(workflowRequest.getPayload()));
        } catch (Exception e) {
            // fallback if payload is invalid JSON
            dto.setPayload(null);
        }
		
		return dto;
	}
	
	private JsonNode parseJson(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(jsonString);
        } catch (Exception e) {
            // Returns an empty object node instead of crashing the whole request
            return objectMapper.createObjectNode();
        }
    }


}
