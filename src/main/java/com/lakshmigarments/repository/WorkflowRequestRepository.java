package com.lakshmigarments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.WorkflowRequest;

@Repository
public interface WorkflowRequestRepository extends JpaRepository<WorkflowRequest, Long>, 
	JpaSpecificationExecutor<WorkflowRequest> {

}
