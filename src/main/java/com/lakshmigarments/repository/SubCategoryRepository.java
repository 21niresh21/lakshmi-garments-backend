package com.lakshmigarments.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.SubCategory;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long>, JpaSpecificationExecutor<SubCategory> {

	Optional<SubCategory> findByName(String name);
		
	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
	
	boolean existsByNameIgnoreCase(String name);

}	
