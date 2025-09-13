package com.lakshmigarments.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.SubCategory;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

	Optional<SubCategory> findByName(String name);
	
	Page<SubCategory> findByNameContainingIgnoreCase(String name, Pageable pageable);
	
	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
	
	boolean existsByNameIgnoreCaseAndCategoryIdAndIdNot(String name, Long categoryId, Long id);

}
