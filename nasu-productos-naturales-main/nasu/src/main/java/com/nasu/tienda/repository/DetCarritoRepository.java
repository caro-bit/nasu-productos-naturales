package com.nasu.tienda.repository;

import com.nasu.tienda.domain.DetCarrito;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetCarritoRepository extends JpaRepository<DetCarrito, Integer> {

    @EntityGraph(attributePaths = "producto")
    public List<DetCarrito> findByIdCarritoAndActivoTrue(Integer idCarrito);

    public Optional<DetCarrito> findByIdCarritoAndIdProductoAndActivoTrue(Integer idCarrito, Integer idProducto);
}
