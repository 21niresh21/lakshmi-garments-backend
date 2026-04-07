package com.lakshmigarments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.JobworkReceiptItem;

@Repository
public interface JobworkReceiptItemRepository extends 
	JpaRepository<JobworkReceiptItem, Long> {

	@Query(value = "SELECT COALESCE(SUM(jwri.accepted_quantity), 0) " +
			"FROM jobwork_receipt_items jwri " +
			"JOIN jobwork_receipts jwr ON jwri.jobwork_receipt_id = jwr.id " +
			"JOIN jobworks jw ON jwr.jobwork_id = jw.id " +
			"JOIN batches b ON jw.batch_id = b.id " +
			"JOIN items i ON jwri.item_id = i.id " +
			"WHERE b.serial_code = :serialCode " +
			"AND jw.jobwork_type = :jobworkType " +
			"AND i.name = :itemName", nativeQuery = true)
	Long getAcceptedQuantityByBatchAndJobworkTypeAndItem(
			@Param("serialCode") String serialCode,
			@Param("jobworkType") String jobworkType,
			@Param("itemName") String itemName);

	@Query(value = "SELECT COALESCE(SUM(jwri.accepted_quantity + jwri.sales_quantity + jwri.damaged_quantity), 0) " +
			"FROM jobwork_receipt_items jwri " +
			"JOIN jobwork_receipts jwr ON jwri.jobwork_receipt_id = jwr.id " +
			"JOIN jobworks jw ON jwr.jobwork_id = jw.id " +
			"JOIN items i ON jwri.item_id = i.id " +
			"WHERE jw.jobwork_number = :jobworkNumber " +
			"AND i.name = :itemName", nativeQuery = true)
	Long getTotalSubmittedQuantityForJobworkItem(
			@Param("jobworkNumber") String jobworkNumber,
			@Param("itemName") String itemName);

	@Query(value = "SELECT COALESCE(SUM(jwri.accepted_quantity + jwri.sales_quantity + jwri.damaged_quantity), 0) " +
			"FROM jobwork_receipt_items jwri " +
			"JOIN jobwork_receipts jwr ON jwri.jobwork_receipt_id = jwr.id " +
			"JOIN jobworks jw ON jwr.jobwork_id = jw.id " +
			"WHERE jw.jobwork_number = :jobworkNumber", nativeQuery = true)
	Long getTotalSubmittedQuantityForJobwork(
			@Param("jobworkNumber") String jobworkNumber);

}
