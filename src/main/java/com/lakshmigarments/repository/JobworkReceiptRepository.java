package com.lakshmigarments.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.dto.PaydayDTO;
import com.lakshmigarments.model.JobworkReceipt;
import com.lakshmigarments.model.JobworkType;

@Repository
public interface JobworkReceiptRepository extends JpaRepository<JobworkReceipt, Long> {

	List<JobworkReceipt> findByJobworkJobworkNumberIn(List<String> jobworkNumbers);
	
	@Query(value = "SELECT COALESCE(SUM(COALESCE(jri.damaged_quantity,0) + COALESCE(jri.purchase_quantity,0) + COALESCE(jri.received_quantity,0)),0) " +
            "FROM jobwork_receipt_items jri JOIN jobwork_receipts jr ON jri.jobwork_receipt_id = jr.id " +
            "WHERE jr.jobwork_id = :jobworkId", nativeQuery = true)
	Long findReturnedUnits(@Param("jobworkId") Long jobworkId);
	
	List<JobworkReceipt> findByJobworkBatchSerialCode(String serialCode);
	
	List<JobworkReceipt> findByJobworkBatchSerialCodeAndJobworkJobworkType(
			String serialCode, JobworkType jobworkType);

	// Get jobwork receipts for CLOSED jobworks, optionally filtered by employee name
	// get the accepted quantity for the jobwork, item, jobwork type
	@Query(value = "SELECT COALESCE(SUM(i.acceptedQuantity), 0) FROM ", nativeQuery = true)
	Long getAcceptedQuantityAndPermanantDamagesForJobwork();
	
	@Query(
		    value = """
		        SELECT jwr.*
		        FROM jobwork_receipts jwr
		        JOIN jobworks jw ON jw.id = jwr.jobwork_id
		        JOIN employees e ON e.id = jw.assigned_to_id
		        WHERE jw.jobwork_status = 'CLOSED'
		          AND (:employeeName IS NULL OR e.name = :employeeName)
		    """,
		    nativeQuery = true
		)
		List<JobworkReceipt> getJobworkReceipts(
		    @Param("employeeName") String employeeName
		);

	
}
