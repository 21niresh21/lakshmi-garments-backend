package com.lakshmigarments.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.lakshmigarments.model.Jobwork;

public interface JobworkRepository extends JpaRepository<Jobwork, Long>, JpaSpecificationExecutor<Jobwork> {

    List<Jobwork> findAllByJobworkNumber(String jobworkNumber);

    @Query("SELECT j FROM Jobwork j WHERE j.id IN (" +
            "SELECT MIN(j2.id) FROM Jobwork j2 GROUP BY j2.jobworkNumber)")
    List<Jobwork> findUniqueJobworksByJobworkNumber();

    Optional<Jobwork> findTop1ByOrderByJobworkNumberDesc();

    List<Jobwork> findByBatchId(Long batchId);

}
