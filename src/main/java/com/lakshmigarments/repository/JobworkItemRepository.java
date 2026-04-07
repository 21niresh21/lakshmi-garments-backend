package com.lakshmigarments.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lakshmigarments.model.Item;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.model.JobworkItem;

public interface JobworkItemRepository extends JpaRepository<JobworkItem, Long> {

	List<JobworkItem> findAllByJobwork(Jobwork jobwork);
	
	Optional<JobworkItem> findByJobworkJobworkNumberAndItem(String jobworkNumber, Item item);
	
	Optional<JobworkItem> findByJobworkJobworkNumber(String jobworkNumber);
	
	@Query(value = "SELECT DISTINCT i.name FROM jobworks jw JOIN jobwork_items jwi ON jwi.jobwork_id = jw.id "
			+ "JOIN items i ON i.id = jwi.item_id WHERE jw.jobwork_number = :jobworkNumber", nativeQuery = true)
	List<String> findItemNamesByJobworkNumber(@Param("jobworkNumber") String jobworkNumber);
	
	Optional<JobworkItem> findByItemNameAndJobworkJobworkNumber(String itemName, String jobworkNumber);
	
}
