package com.lakshmigarments.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
	
	Page<Supplier> findByNameContainingIgnoreCase(String name, Pageable pageable);
	
	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

}
