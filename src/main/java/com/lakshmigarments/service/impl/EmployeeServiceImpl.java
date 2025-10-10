package com.lakshmigarments.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.lakshmigarments.dto.EmployeeRequestDTO;
import com.lakshmigarments.dto.EmployeeResponseDTO;
import com.lakshmigarments.dto.SkillResponseDTO;
import com.lakshmigarments.exception.DuplicateEmployeeException;
import com.lakshmigarments.exception.EmployeeNotFoundException;
import com.lakshmigarments.exception.SkillNotFoundException;
import com.lakshmigarments.model.Employee;
import com.lakshmigarments.model.EmployeeSkill;
import com.lakshmigarments.model.Skill;
import com.lakshmigarments.repository.EmployeeRepository;
import com.lakshmigarments.repository.EmployeeSkillRepository;
import com.lakshmigarments.repository.SkillRepository;
import com.lakshmigarments.repository.specification.EmployeeSpecification;
import com.lakshmigarments.service.EmployeeService;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

	private final Logger LOGGER = LoggerFactory.getLogger(EmployeeServiceImpl.class);

	private final EmployeeRepository employeeRepository;

	private final SkillRepository skillRepository;

	private final EmployeeSkillRepository employeeSkillRepository;

	private final ModelMapper modelMapper;

	@Transactional
	public EmployeeResponseDTO createEmployee(EmployeeRequestDTO employeeRequestDTO) {

		String empName = employeeRequestDTO.getName().trim();
		List<Skill> skills = validateSkillIDs(employeeRequestDTO.getSkills());

		if (employeeRepository.existsByName(empName)) {
			LOGGER.error("Employee already exists with name {}", empName);
			throw new DuplicateEmployeeException("Employee already exists with name " + empName);
		}

		Employee employee = new Employee();
		employee.setName(empName);

		Employee savedEmployee = employeeRepository.save(employee);
		LOGGER.info("Employee saved with ID: {}", savedEmployee.getId());

		if (!skills.isEmpty()) {
			for (Skill skill : skills) {
				EmployeeSkill employeeSkill = new EmployeeSkill();
				employeeSkill.setEmployee(savedEmployee);
				employeeSkill.setSkill(skill);
				employeeSkillRepository.save(employeeSkill);
			}
			LOGGER.info("Associated {} skill(s) to employee ID: {}", skills.size(), savedEmployee.getId());
		}

		EmployeeResponseDTO responseDTO = modelMapper.map(savedEmployee, EmployeeResponseDTO.class);

		List<SkillResponseDTO> skillResponseDTOs = skills.stream()
				.map(skill -> modelMapper.map(skill, SkillResponseDTO.class))
				.collect(Collectors.toList());

		responseDTO.setSkills(skillResponseDTOs);
		return responseDTO;

	}

	// PUT update employee
	@Transactional
	public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO employeeRequestDTO) {
		LOGGER.info("Updating employee with ID: {}", id);

		Employee employee = employeeRepository.findById(id)
				.orElseThrow(() -> {
					LOGGER.error("Employee not found with ID: {}", id);
					return new EmployeeNotFoundException("Employee not found with ID " + id);
				});

		String employeeName = employeeRequestDTO.getName().trim().toLowerCase();

		// Check for duplicate name (excluding current employee)
		if (employeeRepository.existsByNameAndIdNot(employeeName, id)) {
			LOGGER.error("Duplicate employee name detected: '{}'", employeeName);
			throw new DuplicateEmployeeException("Employee already exists with name " + employeeName);
		}

		employee.setName(employeeName);

		// Remove all existing skills for this employee
		LOGGER.debug("Removing all skills associated with employee ID: {}", id);
		employeeSkillRepository.deleteByEmployee(employee);

		// Add new skills
		List<Skill> skills = validateSkillIDs(employeeRequestDTO.getSkills());
		LOGGER.debug("Associating {} new skill(s) to employee ID: {}", skills.size(), id);
		for (Skill skill : skills) {
			EmployeeSkill employeeSkill = new EmployeeSkill();
			employeeSkill.setEmployee(employee);
			employeeSkill.setSkill(skill);
			employeeSkillRepository.save(employeeSkill);
			LOGGER.debug("Associated skill '{}' to employee ID: {}", skill.getName(), id);
		}

		Employee savedEmployee = employeeRepository.save(employee);
		LOGGER.info("Employee updated successfully with ID: {}", savedEmployee.getId());

		EmployeeResponseDTO responseDTO = modelMapper.map(savedEmployee, EmployeeResponseDTO.class);
		List<SkillResponseDTO> skillResponseDTOs = skills.stream()
				.map(skill -> modelMapper.map(skill, SkillResponseDTO.class)).collect(Collectors.toList());
		responseDTO.setSkills(skillResponseDTOs);
		return responseDTO;
	}

	// get all employees
	@Override
	public List<EmployeeResponseDTO> getAllEmployees(String search) {
		Specification<Employee> specification = EmployeeSpecification.filterByName(search);
		List<Employee> employees = employeeRepository.findAll(specification);

		List<EmployeeResponseDTO> employeeResponseDTOs = employees.stream()
				.map(employee -> {
					EmployeeResponseDTO dto = modelMapper.map(employee, EmployeeResponseDTO.class);
					List<EmployeeSkill> employeeSkills = employeeSkillRepository.findByEmployee(employee);
					List<SkillResponseDTO> skillResponseDTOs = employeeSkills.stream()
							.map(employeeSkill -> modelMapper.map(employeeSkill.getSkill(), SkillResponseDTO.class))
							.collect(Collectors.toList());
					dto.setSkills(skillResponseDTOs);
					return dto;
				})
				.collect(Collectors.toList());

		return employeeResponseDTOs;
	}

	// validate skill IDs
	private List<Skill> validateSkillIDs(List<Long> skillIDs) {

		if (skillIDs == null || skillIDs.isEmpty()) {
			LOGGER.info("No skills provided, proceeding with empty skill list.");
			return new ArrayList<Skill>();
		}

		List<Skill> skills = new ArrayList<>();
		for (Long skillID : skillIDs) {
			Skill skill = skillRepository.findById(skillID)
					.orElseThrow(() -> new SkillNotFoundException("Skill not found with ID " + skillID));
			skills.add(skill);
		}
		return skills;
	}

}
