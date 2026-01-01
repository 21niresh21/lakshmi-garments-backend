package com.lakshmigarments.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.JobworkReceipt;

@Repository
public interface JobworkReceiptRepository extends JpaRepository<JobworkReceipt, Long> {

	List<JobworkReceipt> findByJobworkJobworkNumberIn(List<String> jobworkNumbers);
	
}
