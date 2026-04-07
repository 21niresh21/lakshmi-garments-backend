package com.lakshmigarments.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.SubCategory;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long>, JpaSpecificationExecutor<SubCategory> {

	Optional<SubCategory> findByName(String name);
	
	// when updating the value
	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
	
	// when creating
	boolean existsByNameIgnoreCase(String name);
	
//	List<SubCategory> findByCategoryId(Long categoryId);

}	
