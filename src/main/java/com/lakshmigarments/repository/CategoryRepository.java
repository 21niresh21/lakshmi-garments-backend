package com.lakshmigarments.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

	Optional<Category> findByName(String name);
	
	// Custom query to find the code of a category by its name
    @Query("SELECT c.code FROM Category c WHERE c.name = :categoryName")
    Optional<String> findCodeByName(String categoryName);
    
    Boolean existsByNameIgnoreCase(String name);
    
    Boolean existsByCodeIgnoreCase(String code);
    
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

}
