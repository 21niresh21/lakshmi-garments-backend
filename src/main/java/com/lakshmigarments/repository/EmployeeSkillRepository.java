package com.lakshmigarments.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lakshmigarments.model.Employee;
import com.lakshmigarments.model.EmployeeSkill;
import com.lakshmigarments.model.Skill;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {

	boolean existsByEmployeeAndSkill(Employee employee, Skill skill);

	void deleteByEmployee(Employee employee);

	List<EmployeeSkill> findByEmployee(Employee employee);
}
