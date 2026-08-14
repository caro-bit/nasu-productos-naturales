package com.nasu.tienda.repository;

import com.nasu.tienda.domain.Rol;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    //Consulta derivada para listar los roles disponibles en orden alfabético
    public List<Rol> findAllByOrderByRolAsc();
}
