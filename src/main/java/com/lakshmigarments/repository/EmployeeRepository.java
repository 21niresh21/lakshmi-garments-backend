package com.lakshmigarments.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

	boolean existsByNameAndIdNot(String name, Long id);

	boolean existsByName(String name);
	
	Optional<Employee> findByName(String name);
	
	@Query("""
	        SELECT DISTINCT e FROM Employee e
	        LEFT JOIN EmployeeSkill es ON es.employee.id = e.id
	        LEFT JOIN Skill s ON es.skill.id = s.id
	        WHERE (:employeeNames IS NULL OR e.name IN :employeeNames)
	        AND (:skillNames IS NULL OR s.name IN :skillNames)
	        AND (:isActive IS NULL OR e.isActive = :isActive)
	        AND (
	            :search IS NULL OR
	            LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))
	        )
	    """)
	    Page<Employee> findEmployees(
	            @Param("employeeNames") List<String> employeeNames,
	            @Param("skillNames") List<String> skillNames,
	            @Param("isActive") Boolean isActive,
	            @Param("search") String search,
	            Pageable pageable);

}
