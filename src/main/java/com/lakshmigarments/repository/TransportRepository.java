package com.lakshmigarments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.Transport;

@Repository
public interface TransportRepository extends JpaRepository<Transport, Long> {
	
	Boolean existsByName(String name);

}
