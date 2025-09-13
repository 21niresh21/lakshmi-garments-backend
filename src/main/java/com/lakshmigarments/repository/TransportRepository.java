package com.lakshmigarments.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.Transport;

@Repository
public interface TransportRepository extends JpaRepository<Transport, Long> {
	
	Boolean existsByNameIgnoreCase(String name);
	
	Page<Transport> findByNameContainingIgnoreCase(String name, Pageable pageable);
	
	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

}
