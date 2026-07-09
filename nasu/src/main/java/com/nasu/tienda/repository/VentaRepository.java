package com.nasu.tienda.repository;

import com.nasu.tienda.domain.Venta;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    @EntityGraph(attributePaths = "producto")
    public List<Venta> findByIdFactura(Integer idFactura);
}