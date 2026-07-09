package com.nasu.tienda.repository;

import com.nasu.tienda.domain.Carrito;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Integer> {

    public Optional<Carrito> findByIdUsuarioAndActivoTrue(Integer idUsuario);
}
