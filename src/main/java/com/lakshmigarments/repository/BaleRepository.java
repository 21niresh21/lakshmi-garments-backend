package com.lakshmigarments.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lakshmigarments.model.Bale;

@Repository
public interface BaleRepository extends JpaRepository<Bale, Long> {

    Optional<Bale> findByLorryReceiptId(Long lorryReceiptId);

}
