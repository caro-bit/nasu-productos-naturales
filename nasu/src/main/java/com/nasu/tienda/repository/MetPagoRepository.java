package com.nasu.tienda.repository;

import com.nasu.tienda.domain.MetPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetPagoRepository extends JpaRepository<MetPago, Integer> {
}