package com.lakshmigarments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long>, 
	JpaSpecificationExecutor<Supplier> {
	
	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
	
	boolean existsByNameIgnoreCase(String name);

}
