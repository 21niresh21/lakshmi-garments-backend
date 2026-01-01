package com.lakshmigarments.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.lakshmigarments.model.Jobwork;

public interface JobworkRepository extends JpaRepository<Jobwork, Long>, JpaSpecificationExecutor<Jobwork> {

	Optional<Jobwork> findByJobworkNumber(String jobworkNumber);	

	@Query("SELECT j FROM Jobwork j WHERE j.id IN (" + "SELECT MIN(j2.id) FROM Jobwork j2 GROUP BY j2.jobworkNumber)")
	List<Jobwork> findUniqueJobworksByJobworkNumber();

	Optional<Jobwork> findTop1ByOrderByJobworkNumberDesc();

	List<Jobwork> findByBatchId(Long batchId);

	@Query(value = "SELECT COUNT(*) FROM jobworks jw WHERE jw.employee_id = :employeeId AND jw.ended_at <> NULL", nativeQuery = true)
	Long findActiveJobworkCount(Long employeeId);
	
	@Query(value = "SELECT COALESCE(SUM(quantity), 0) FROM jobworks jw WHERE jw.employee_id = :employeeId AND jw.ended_at IS NULL", nativeQuery = true)
	Long findLifetimePiecesHandled(Long employeeId);
	
	Page<Jobwork> findByJobworkNumberContainingIgnoreCase(
            String jobworkNumber,
            Pageable pageable
    );

}
