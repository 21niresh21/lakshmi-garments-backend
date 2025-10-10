package com.lakshmigarments.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.lakshmigarments.dto.EmployeeRequestDTO;
import com.lakshmigarments.dto.EmployeeResponseDTO;
import com.lakshmigarments.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/employees")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@AllArgsConstructor
public class EmployeeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeController.class);

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees(@RequestParam(required = false) String search) {
        LOGGER.info("Received request to get all employees");
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees(search);
        LOGGER.info("Employees retrieved successfully");
        return ResponseEntity.status(HttpStatus.OK).body(employees);
    }
    
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @Valid @RequestBody EmployeeRequestDTO employeeRequestDTO) {
        LOGGER.info("Received request to create employee: {}", employeeRequestDTO.getName());

        EmployeeResponseDTO responseDTO = employeeService.createEmployee(employeeRequestDTO);

        LOGGER.info("Employee created successfully with ID: {}", responseDTO.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO employeeRequestDTO) {
        LOGGER.info("Received request to update employee with ID: {}", id);
        EmployeeResponseDTO responseDTO = employeeService.updateEmployee(id, employeeRequestDTO);
        LOGGER.info("Employee updated successfully with ID: {}", id);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

}
