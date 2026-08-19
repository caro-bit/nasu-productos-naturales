package com.nasu.tienda.repository;

import com.nasu.tienda.domain.Rol;
import com.nasu.tienda.domain.Ruta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    
    List<Rol> findAllByOrderByRolAsc();
    
    public Optional<Rol> findByRol(String rol);

}