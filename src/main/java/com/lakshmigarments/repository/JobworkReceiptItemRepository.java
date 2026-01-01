package com.lakshmigarments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.JobworkReceiptItem;

@Repository
public interface JobworkReceiptItemRepository extends 
	JpaRepository<JobworkReceiptItem, Long> {

}
