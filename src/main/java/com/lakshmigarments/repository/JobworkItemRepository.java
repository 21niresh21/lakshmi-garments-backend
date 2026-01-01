package com.lakshmigarments.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lakshmigarments.model.Item;
import com.lakshmigarments.model.Jobwork;
import com.lakshmigarments.model.JobworkItem;

public interface JobworkItemRepository extends JpaRepository<JobworkItem, Long> {

	List<JobworkItem> findAllByJobwork(Jobwork jobwork);
	
	Optional<JobworkItem> findByJobworkJobworkNumberAndItem(String jobworkNumber, Item item);
	
	Optional<JobworkItem> findByJobworkJobworkNumber(String jobworkNumber);
	
}
