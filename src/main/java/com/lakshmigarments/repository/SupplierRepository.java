package com.lakshmigarments.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long>, 
	JpaSpecificationExecutor<Supplier> {
	
	Boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
	
	Boolean existsByNameIgnoreCase(String name);
	
	Optional<Supplier> findByNameIgnoreCase(String name);

}
