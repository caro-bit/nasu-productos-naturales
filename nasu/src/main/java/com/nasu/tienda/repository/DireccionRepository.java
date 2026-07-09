package com.nasu.tienda.repository;

import com.nasu.tienda.domain.Direccion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Integer> {

    public List<Direccion> findByIdUsuario(Integer idUsuario);
}