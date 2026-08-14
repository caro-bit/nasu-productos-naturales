package com.nasu.tienda.repository;

import com.nasu.tienda.domain.Ruta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Integer> {

    //Las rutas públicas se registran primero, porque en Spring Security gana la
    //primera regla que coincide con la dirección solicitada.
    public List<Ruta> findAllByOrderByRequiereRolAsc();
}
