package com.lakshmigarments.controller;

import com.lakshmigarments.dto.JobworkTimelineResponse;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.JobworkRequestDTO;
import com.lakshmigarments.dto.JobworkResponseDTO;
import com.lakshmigarments.dto.request.CreateJobworkRequest;
import com.lakshmigarments.dto.request.ReassignJobworkRequest;
import com.lakshmigarments.dto.response.EmployeeJobworkReportResponse;
import com.lakshmigarments.dto.response.EmployeeJobworkResponse;
import com.lakshmigarments.dto.response.ItemResponse;
import com.lakshmigarments.dto.response.JobworkDetailDTO;
import com.lakshmigarments.dto.response.JobworkResponse;
import com.lakshmigarments.dto.response.PriorJobworkResponse;
import com.lakshmigarments.model.Employee;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.model.JobworkStatus;
import com.lakshmigarments.model.JobworkType;
import com.lakshmigarments.service.JobworkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/jobworks")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class JobworkController {

	private final Logger LOGGER = LoggerFactory.getLogger(JobworkController.class);
	private final JobworkService<CreateJobworkRequest> jobworkService;

	@GetMapping
	public ResponseEntity<Page<JobworkResponseDTO>> getAllJobworks(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "7") int size, @RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String order, @RequestParam(required = false) String search,
			// Match these names to your Frontend query keys
			@RequestParam(required = false) List<String> assignedToName,
			@RequestParam(required = false) List<JobworkStatus> jobworkStatus,
			@RequestParam(required = false) List<JobworkType> jobworkType,
			@RequestParam(required = false) List<String> batchSerialCode,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

		LOGGER.error("start {} and end {}", startDate, endDate);

		Sort sort = order.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
		Pageable pageable = PageRequest.of(page, size, sort);

		// Pass the controller variables into the service
		// Note: ensure the order here matches your JobworkService interface exactly
		return ResponseEntity.ok(jobworkService.getAllJobworks(pageable, search, assignedToName, // maps to
																									// assignedToNames
																									// in service
				jobworkStatus, // maps to statuses in service
				jobworkType, // maps to types in service
				batchSerialCode, // maps to batchSerialCodes in service
				startDate, endDate));
	}

	// create a new jobwork
	@PostMapping
	public ResponseEntity<JobworkResponse> createJobwork(@RequestBody CreateJobworkRequest jobworkRequest) {
		LOGGER.info("Received jobwork request for batch: {} and employee: {}", jobworkRequest.getBatchSerialCode(),
				jobworkRequest.getAssignedTo());
		JobworkResponse createdJobwork = jobworkService.createJobwork(jobworkRequest);
		LOGGER.info("Jobwork created successfully for batch: {} and employee: {}", jobworkRequest.getBatchSerialCode(),
				jobworkRequest.getAssignedTo());
		return new ResponseEntity<>(createdJobwork, HttpStatus.CREATED);
	}

	@GetMapping("/jobwork-numbers")
	public ResponseEntity<List<String>> getJobworkNumbers(@RequestParam(required = false) String search) {
		return new ResponseEntity<>(jobworkService.getJobworkNumbers(search), HttpStatus.OK);
	}

	// GETTNG a quick detail of the jobwork for viewing during in pass acceptance
	@GetMapping("/jobwork-numbers/{jobworkNumber}")
	public ResponseEntity<JobworkDetailDTO> getJobworkDetail(@PathVariable String jobworkNumber) {
		LOGGER.info("Received request to get jobwork details of {}", jobworkNumber);
		return new ResponseEntity<>(jobworkService.getJobworkDetail(jobworkNumber), HttpStatus.OK);
	}

	@GetMapping("/jobwork-numbers/next-number")
	public ResponseEntity<String> getNextJobworkNumber() {
		return new ResponseEntity<>(jobworkService.getNextJobworkNumber(), HttpStatus.OK);
	}

	@PostMapping("/{jobworkNumber}/reassign")
	public ResponseEntity<Jobwork> reAssignJobwork(@PathVariable String jobworkNumber,
			@RequestBody ReassignJobworkRequest reassignRequest) {
		LOGGER.info("Received request to reassign jobwork {} to employee {}", jobworkNumber,
				reassignRequest.getEmployeeName());
		return new ResponseEntity<>(jobworkService.reAssignJobwork(jobworkNumber, reassignRequest.getEmployeeName()),
				HttpStatus.OK);
	}

	@PostMapping("/{jobworkNumber}/close")
	public ResponseEntity<JobworkResponse> closeJobwork(@PathVariable String jobworkNumber) {
		LOGGER.info("Received request to close jobwork {}", jobworkNumber);
		JobworkResponse jobworkResponse = jobworkService.closeJobwork(jobworkNumber);
		LOGGER.info("Closed jobwork {}", jobworkNumber);
		return new ResponseEntity<>(jobworkResponse, HttpStatus.OK);
	}

	@PostMapping("/{jobworkNumber}/reopen")
	public ResponseEntity<JobworkResponse> reopenJobwork(@PathVariable String jobworkNumber) {
		LOGGER.info("Received request to reopen jobwork {}", jobworkNumber);
		JobworkResponse jobworkResponse = jobworkService.reopenJobwork(jobworkNumber);
		LOGGER.info("Reopened jobwork {}", jobworkNumber);
		return new ResponseEntity<>(jobworkResponse, HttpStatus.OK);
	}

	@GetMapping("/{jobworkNumber}/items")
	public ResponseEntity<List<ItemResponse>> getItemsForJobwork(@PathVariable String jobworkNumber) {
		LOGGER.info("Received request to get items for jobwork {}", jobworkNumber);
		List<ItemResponse> items = jobworkService.getItemsForJobwork(jobworkNumber);
		LOGGER.info("Received items {} for jobwork {}", items.size(), jobworkNumber);
		return new ResponseEntity<>(items, HttpStatus.OK);
	}

	@GetMapping("/employee/{employeeName}")
	public ResponseEntity<List<EmployeeJobworkResponse>> getJobworksByEmployee(
			@PathVariable String employeeName) {
		LOGGER.info("Received request to get all jobworks for employee: {}", employeeName);
		List<EmployeeJobworkResponse> jobworks = jobworkService.getJobworksByEmployeeName(employeeName);
		LOGGER.info("Found {} jobworks for employee: {}", jobworks.size(), employeeName);
		return new ResponseEntity<>(jobworks, HttpStatus.OK);
	}

	@GetMapping("/employee/{employeeName}/detailed")
	public ResponseEntity<EmployeeJobworkReportResponse> getDetailedJobworksByEmployee(
			@PathVariable String employeeName,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
		LOGGER.info("Received request for detailed jobwork report for employee: {} ({} to {})", 
				employeeName, startDate, endDate);
		EmployeeJobworkReportResponse report = jobworkService.getDetailedJobworksByEmployee(employeeName, startDate, endDate);
		return new ResponseEntity<>(report, HttpStatus.OK);
	}

	@GetMapping("/timeline/{jobworkNumber}")
	public ResponseEntity<JobworkTimelineResponse> getJobworkTimeline(@PathVariable String jobworkNumber) {
		LOGGER.info("Received request for jobwork timeline of {}", jobworkNumber);
		JobworkTimelineResponse timeline = jobworkService.getJobworkTimeline(jobworkNumber);
		return ResponseEntity.ok(timeline);
	}

	@GetMapping("/{jobworkNumber}/prior-closed")
	public ResponseEntity<List<PriorJobworkResponse>> getPriorClosedJobworks(@PathVariable String jobworkNumber) {
		LOGGER.info("Received request for prior closed jobworks for {}", jobworkNumber);
		List<PriorJobworkResponse> priorJobworks = jobworkService.getPriorClosedJobworks(jobworkNumber);
		return ResponseEntity.ok(priorJobworks);
	}

}
