package com.lakshmigarments.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lakshmigarments.model.Batch;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    Boolean existsBySerialCode(String serialCode);

    // JPQL query to get the latest serial code for a given category
    @Query("SELECT b.serialCode FROM Batch b WHERE b.category.name = :categoryName ORDER BY b.createdAt DESC LIMIT 1")
    Optional<String> findLatestSerialCodeByCategoryName(String categoryName);

    @Query("SELECT b FROM Batch b WHERE LOWER(b.serialCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY b.createdAt DESC ")
    List<Batch> findBySerialCodeContaining(@Param("searchTerm") String searchTerm);

    // JPA query to get all batches that are not packed yet from the jobwork_types
    // table
    @Query("""
                SELECT b FROM Batch b
                WHERE b.id NOT IN (
                    SELECT jw.batch.id
                    FROM Jobwork jw
                    WHERE jw.jobworkType.name = 'Packaging'
                )
            """)
    List<Batch> findUnpackagedBatches();
}
