package com.lakshmigarments.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lakshmigarments.model.Batch;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.model.JobworkStatus;
import com.lakshmigarments.model.JobworkType;

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

	Page<Jobwork> findByJobworkNumberContainingIgnoreCase(String jobworkNumber, Pageable pageable);

	List<Jobwork> findByBatchSerialCode(String serialCode);

	List<Jobwork> findByBatchSerialCodeAndJobworkStatusIn(String serialCode, List<JobworkStatus> jobworkStatuses);

	@Query(value = "SELECT COALESCE(SUM(jwi.quantity), 0) FROM jobworks jw, jobwork_items jwi WHERE jw.jobwork_number = :jobworkNumber "
			+ "AND jw.id = jwi.jobwork_id", nativeQuery = true)
	Long findTotalQuantities(String jobworkNumber);

	// to get jobworks for the batch by jobwork type
	List<Jobwork> findByBatchSerialCodeAndJobworkType(String serialCode, JobworkType jobworkType);

	@Query(value = "SELECT COALESCE(SUM(jwi.quantity), 0) FROM jobworks jw, jobwork_items jwi, batches b "
			+ " WHERE jw.id = jwi.jobwork_id AND jw.batch_id = b.id AND b.serial_code = :serialCode and jw.jobwork_type = :jobworkType AND jw.jobwork_status <> 'REASSIGNED'", nativeQuery = true)
	Long getAssignedQuantities(String serialCode, String jobworkType);

	@Query(value = "SELECT COALESCE(SUM(jwi.quantity), 0) FROM jobworks jw, jobwork_items jwi, batches b, items i "
			+ " WHERE jw.id = jwi.jobwork_id AND jw.batch_id = b.id AND b.serial_code = :serialCode and jw.jobwork_type = :jobworkType and jwi.item_id = i.id and i.name = :itemName AND jw.jobwork_status <> 'REASSIGNED'", nativeQuery = true)
	Long getAssignedQuantities(String serialCode, String jobworkType, String itemName);
	
	@Query(value = "SELECT COALESCE(SUM(jwi.quantity), 0) FROM jobworks jw, jobwork_items jwi, batches b, items i "
			+ " WHERE jw.id = jwi.jobwork_id AND jw.batch_id = b.id AND b.serial_code = :serialCode and jw.jobwork_type = :jobworkType and jwi.item_id = i.id and i.name = :itemName AND (jw.jobwork_status = 'REASSIGNED' OR jw.jobwork_status = 'IN_PROGRESS')", nativeQuery = true)
	Long getAssignedQuantitiesInProgress(String serialCode, String jobworkType, String itemName);

	@Query(value = "SELECT COALESCE(SUM(jwi.quantity), 0) FROM jobworks jw, jobwork_items jwi, batches b, sub_categories sc "
			+ " WHERE jw.id = jwi.jobwork_id AND jw.batch_id = b.id AND b.serial_code = :serialCode and jw.jobwork_type = :jobworkType and jwi.sub_category_id = sc.id and sc.name = :subCategoryName AND jw.jobwork_status <> 'REASSIGNED'", nativeQuery = true)
	Long getAssignedQuantitiesBySubCategory(String serialCode, String jobworkType, String subCategoryName);

	List<Jobwork> findByBatch(Batch batch);

	// Find all child jobworks (reassigned/split from a parent)
	List<Jobwork> findByParentJobwork(Jobwork parentJobwork);

	// Find all jobworks assigned to a specific employee by name
	List<Jobwork> findByAssignedToNameOrderByCreatedAtDesc(String employeeName);

	// Find closed jobworks created before a specific date (for damage source selection)
	@Query("SELECT j FROM Jobwork j WHERE j.createdAt < :createdAt AND j.jobworkStatus = 'CLOSED' ORDER BY j.createdAt DESC")
	List<Jobwork> findClosedJobworksCreatedBefore(@Param("createdAt") LocalDateTime createdAt);

	// Count pending jobworks (not CLOSED or REASSIGNED) for an employee
	@Query("SELECT COUNT(j) FROM Jobwork j WHERE j.assignedTo.name = :employeeName " +
			"AND j.jobworkStatus NOT IN ('CLOSED', 'REASSIGNED')")
	Long countPendingJobworksByEmployeeName(@Param("employeeName") String employeeName);

	@Query("SELECT j.jobworkNumber FROM Jobwork j WHERE j.assignedTo.name = :employeeName " +
			"AND j.jobworkStatus NOT IN ('CLOSED', 'REASSIGNED') " +
			"AND (:startDate IS NULL OR j.createdAt >= :startDate) " +
			"AND (:endDate IS NULL OR j.createdAt <= :endDate)")
	List<String> findPendingJobworkNumbersByEmployeeNameAndDateRange(
			@Param("employeeName") String employeeName,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	// Get list of pending jobwork numbers for an employee (without date filter)
	@Query("SELECT j.jobworkNumber FROM Jobwork j WHERE j.assignedTo.name = :employeeName " +
			"AND j.jobworkStatus NOT IN ('CLOSED', 'REASSIGNED')")
	List<String> findPendingJobworkNumbersByEmployeeName(@Param("employeeName") String employeeName);

	// Get total cutting quantity issued for a batch (for dynamic validation)
	@Query(value = "SELECT COALESCE(SUM(jwi.quantity), 0) FROM jobworks jw, jobwork_items jwi " +
			"WHERE jw.id = jwi.jobwork_id AND jw.batch_id = :batchId AND jw.jobwork_type = 'CUTTING' " +
			"AND jw.jobwork_status <> 'REASSIGNED'", nativeQuery = true)
	Long getTotalCuttingQuantityIssued(@Param("batchId") Long batchId);

	// return a boolean if there are any jobworks issued for the batch based on batch serial code in any of 
	// the given jobwork types - STITCHING, EMBROIDERY, PACKAGING
	@Query(value = "SELECT CASE WHEN COUNT(jw) > 0 THEN true ELSE false END FROM Jobwork jw WHERE jw.batch.serialCode = :serialCode AND jw.jobworkType IN ('STITCHING', 'EMBROIDERY', 'PACKAGING', 'OVERLOCK', 'IRONING') AND jw.jobworkStatus <> 'REASSIGNED'")
	boolean existsByBatchSerialCodeForItem(@Param("serialCode") String serialCode);

	// Find all jobworks in a batch created before a specific date
	List<Jobwork> findByBatchSerialCodeAndCreatedAtBefore(String serialCode, LocalDateTime createdAt);

}
