package com.lakshmigarments.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lakshmigarments.model.Damage;

public interface DamageRepository extends JpaRepository<Damage, Long> {

    @Query("SELECT d FROM Damage d WHERE d.jobWork.batch.id = :batchId")
    List<Damage> findAllByBatchId(@Param("batchId") Long batchId);

}
