package com.lakshmigarments.service;

import java.util.List;
import org.springframework.data.domain.Page;
import com.lakshmigarments.dto.request.EmployeeRequest;
import com.lakshmigarments.dto.response.EmployeeResponse;
import com.lakshmigarments.dto.EmployeeStatsDTO;

public interface EmployeeService {
	
	EmployeeResponse createEmployee(EmployeeRequest employeeRequest);
	
	EmployeeResponse updateEmployee(Long id, EmployeeRequest employeeRequest);

	Page<EmployeeResponse> getEmployees(
	        Integer pageNo,
	        Integer pageSize,
	        String sortBy,
	        String sortOrder,
	        List<String> employeeNames,
	        List<String> skillNames,
	        Boolean isActive,
	        String search);

	EmployeeStatsDTO getEmployeeStats(Long employeeId);

}
