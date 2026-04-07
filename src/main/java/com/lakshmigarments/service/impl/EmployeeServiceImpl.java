package com.lakshmigarments.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lakshmigarments.dto.request.EmployeeRequest;
import com.lakshmigarments.dto.response.EmployeeResponse;
import com.lakshmigarments.dto.response.SkillResponse;
import com.lakshmigarments.dto.EmployeeStatsDTO;
import com.lakshmigarments.exception.DuplicateEmployeeException;
import com.lakshmigarments.exception.EmployeeNotFoundException;
import com.lakshmigarments.exception.SkillNotFoundException;
import com.lakshmigarments.model.Employee;
import com.lakshmigarments.model.EmployeeSkill;
import com.lakshmigarments.model.Skill;
import com.lakshmigarments.repository.EmployeeRepository;
import com.lakshmigarments.repository.EmployeeSkillRepository;
import com.lakshmigarments.repository.JobworkRepository;
import com.lakshmigarments.repository.SkillRepository;
import com.lakshmigarments.service.EmployeeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeServiceImpl.class);

	private final EmployeeRepository employeeRepository;
	private final SkillRepository skillRepository;
	private final EmployeeSkillRepository employeeSkillRepository;
	private final JobworkRepository jobworkRepository;
	private final ModelMapper modelMapper;

	@Override
	@Transactional
	public EmployeeResponse createEmployee(EmployeeRequest employeeRequest) {
		LOGGER.debug("Creating employee: {}", employeeRequest.getName());
		String empName = employeeRequest.getName().trim();
		List<Skill> skills = validateSkillIDs(employeeRequest.getSkills());

		validateEmployeeUniqueness(empName, null);

		Employee employee = new Employee();
		employee.setName(empName);
		employee.setIsActive(true);

		Employee savedEmployee = employeeRepository.save(employee);
		LOGGER.info("Employee created successfully with ID: {}", savedEmployee.getId());

		associateSkillsToEmployee(savedEmployee, skills);

		return mapToResponse(savedEmployee, skills);
	}

	@Override
	@Transactional
	public EmployeeResponse updateEmployee(Long id, EmployeeRequest employeeRequest) {
		LOGGER.debug("Updating employee with ID: {}", id);
		Employee employee = this.getEmployeeOrThrow(id);

		String empName = employeeRequest.getName().trim();
		List<Skill> skills = validateSkillIDs(employeeRequest.getSkills());

		validateEmployeeUniqueness(empName, id);

		employee.setName(empName);

		// Skill update strategy: clear and re-associate
		employeeSkillRepository.deleteByEmployee(employee);
		employeeSkillRepository.flush(); // Ensure deletion before additions
		
		associateSkillsToEmployee(employee, skills);

		Employee savedEmployee = employeeRepository.save(employee);
		LOGGER.info("Employee updated successfully with ID: {}", savedEmployee.getId());

		return mapToResponse(savedEmployee, skills);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<EmployeeResponse> getEmployees(
	        Integer pageNo,
	        Integer pageSize,
	        String sortBy,
	        String sortOrder,
	        List<String> employeeNames,
	        List<String> skillNames,
	        Boolean isActive,
	        String search) {

	    int page = pageNo != null ? pageNo : 0;
	    int size = pageSize != null ? pageSize : 10;
	    Sort sort = (sortOrder != null && sortOrder.equalsIgnoreCase("desc"))
	            ? Sort.by(sortBy).descending()
	            : Sort.by(sortBy).ascending();

	    Pageable pageable = PageRequest.of(page, size, sort);

	    Page<Employee> employees = employeeRepository.findEmployees(
	            employeeNames,
	            skillNames,
	            isActive,
	            search,
	            pageable
	    );

	    return employees.map(this::mapToResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public EmployeeStatsDTO getEmployeeStats(Long employeeId) {
		Employee employee = this.getEmployeeOrThrow(employeeId);
		
		long activeJobs = jobworkRepository.findActiveJobworkCount(employeeId);
		long lifetimePieces = jobworkRepository.findLifetimePiecesHandled(employeeId);
		
		EmployeeStatsDTO stats = new EmployeeStatsDTO();
		stats.setEmployeeName(employee.getName());
		stats.setHasOtherJobs(activeJobs > 0);
		stats.setLifetimePieces(lifetimePieces);
		
		return stats;
	}

	private void validateEmployeeUniqueness(String name, Long id) {
		if (id == null) {
			if (employeeRepository.existsByName(name)) {
				LOGGER.error("Employee name already exists: {}", name);
				throw new DuplicateEmployeeException("Employee already exists with name: " + name);
			}
		} else {
			if (employeeRepository.existsByNameAndIdNot(name, id)) {
				LOGGER.error("Employee name already exists for another ID: {}", name);
				throw new DuplicateEmployeeException("Employee already exists with name: " + name);
			}
		}
	}

	private List<Skill> validateSkillIDs(List<Long> skillIDs) {
		if (skillIDs == null || skillIDs.isEmpty()) {
			return new ArrayList<>();
		}
		List<Skill> skills = new ArrayList<>();
		for (Long id : skillIDs) {
			System.out.println("skill id" + id);
			Skill skill = skillRepository.findById(id)
					.orElseThrow(() -> new SkillNotFoundException("Skill not found with ID: " + id));
			skills.add(skill);
		}
		return skills;
	}

	private void associateSkillsToEmployee(Employee employee, List<Skill> skills) {
		if (skills == null || skills.isEmpty()) return;
		for (Skill skill : skills) {
			EmployeeSkill employeeSkill = new EmployeeSkill();
			employeeSkill.setEmployee(employee);
			employeeSkill.setSkill(skill);
			employeeSkillRepository.save(employeeSkill);
		}
	}

	private EmployeeResponse mapToResponse(Employee employee) {
		List<Skill> skills = (employee.getEmployeeSkills() != null)
				? employee.getEmployeeSkills().stream().map(EmployeeSkill::getSkill).collect(Collectors.toList())
				: new ArrayList<>();
		return mapToResponse(employee, skills);
	}

	private EmployeeResponse mapToResponse(Employee employee, List<Skill> skills) {
		EmployeeResponse response = modelMapper.map(employee, EmployeeResponse.class);
		if (skills != null) {
			response.setSkills(skills.stream()
					.map(s -> modelMapper.map(s, SkillResponse.class))
					.collect(Collectors.toList()));
		}
		return response;
	}

	private Employee getEmployeeOrThrow(Long id) {
		return employeeRepository.findById(id).orElseThrow(() -> {
			LOGGER.error("Employee not found with ID: {}", id);
			return new EmployeeNotFoundException("Employee not found with ID: " + id);
		});
	}

}
