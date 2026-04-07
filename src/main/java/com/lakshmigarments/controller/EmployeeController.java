package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lakshmigarments.dto.request.EmployeeRequest;
import com.lakshmigarments.dto.response.EmployeeResponse;
import com.lakshmigarments.dto.EmployeeStatsDTO;
import com.lakshmigarments.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeController.class);

	private final EmployeeService employeeService;

	@GetMapping
	public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(
			@RequestParam(required = false, defaultValue = "0") Integer pageNo,
			@RequestParam(required = false, defaultValue = "10") Integer pageSize,
			@RequestParam(required = false, defaultValue = "id") String sortBy,
			@RequestParam(required = false, defaultValue = "asc") String sortOrder,
			@RequestParam(required = false) List<String> employeeNames,
			@RequestParam(required = false) List<String> skillNames,
			@RequestParam(required = false) Boolean isActive,
			@RequestParam(required = false) String search) {
		LOGGER.info("Received request to fetch all employees matching search: {}", search);
		Page<EmployeeResponse> employees = employeeService.getEmployees(
				pageNo, pageSize, sortBy, sortOrder, employeeNames, skillNames, isActive, search);
		return ResponseEntity.ok(employees);
	}

	@PostMapping
	public ResponseEntity<EmployeeResponse> createEmployee(
			@RequestBody @Valid EmployeeRequest request) {
		LOGGER.info("Received request to create employee: {}", request.getName());
		EmployeeResponse response = employeeService.createEmployee(request);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id,
			@RequestBody @Valid EmployeeRequest request) {
		LOGGER.info("Received request to update employee ID: {}", id);
		EmployeeResponse response = employeeService.updateEmployee(id, request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/stats/{id}")
	public ResponseEntity<EmployeeStatsDTO> getEmployeeStats(@PathVariable Long id) {
		LOGGER.info("Received request to fetch stats for employee ID: {}", id);
		EmployeeStatsDTO stats = employeeService.getEmployeeStats(id);
		return ResponseEntity.ok(stats);
	}

}
