package com.lakshmigarments.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.EmployeeRequestDTO;
import com.lakshmigarments.dto.EmployeeResponseDTO;

@Service
public interface EmployeeService {
	
	EmployeeResponseDTO createEmployee(EmployeeRequestDTO employeeRequestDTO);
	
	EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO employeeRequestDTO);

	List<EmployeeResponseDTO> getAllEmployees(String search);

}
