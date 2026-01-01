package com.lakshmigarments.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import com.lakshmigarments.model.Batch;

public interface BatchRepository extends JpaRepository<Batch, Long>, JpaSpecificationExecutor<Batch> {

	Boolean existsBySerialCode(String serialCode);

	Optional<Batch> findBySerialCode(String serialCode);

//	@Query(value = "SELECT count(*) FROM batches b, jobworks jw, damages d WHERE "
//			+ "b.serial_code = :serialCode AND b.id = jw.id AND jw.id = d."
//			)

	@Query(value = "SELECT COALESCE(SUM(quantity),0) FROM batch_sub_categories "
			+ "WHERE serial_code = :serialCode", nativeQuery = true)
	Long findQuantityBySerialCode(@Param("serialCode") String serialCode);

	// JPQL query to get the latest serial code for a given category
	@Query("SELECT b.serialCode FROM Batch b WHERE b.category.name = :categoryName ORDER BY b.createdAt DESC LIMIT 1")
	Optional<String> findLatestSerialCodeByCategoryName(String categoryName);

	@Query("SELECT b FROM Batch b WHERE LOWER(b.serialCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY b.createdAt DESC ")
	List<Batch> findBySerialCodeContaining(@Param("searchTerm") String searchTerm);

	// JPA query to get all batches that are not packed yet from the jobwork_types
	// table
	@Query(value = """
			SELECT DISTINCT b.* FROM batches b LEFT JOIN jobworks jw ON jw.batch_id = b.id
			WHERE  b.batch_status <> 'DISCARDED' AND NOT (b.batch_status = 'CLOSED'
			    AND NOT EXISTS ( SELECT 1 FROM damages d WHERE d.reported_from_id = jw.id
			          AND d.damage_type = 'REPAIRABLE'
			    )
			)
			""", nativeQuery = true)
	List<Batch> findAllExceptPackagedWithoutRepairableDamages();
	
	

}
