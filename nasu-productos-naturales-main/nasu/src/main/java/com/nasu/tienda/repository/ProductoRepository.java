package com.nasu.tienda.repository;

import com.nasu.tienda.domain.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    public List<Producto> findByActivoTrue();

    //Consulta derivada para los productos destacados de la página de inicio
    public List<Producto> findTop4ByActivoTrue();

    //Consulta derivada para buscar productos activos por nombre
    public List<Producto> findByDescripcionContainingIgnoreCaseAndActivoTrue(String descripcion);

    //Consulta derivada para filtrar productos activos por categoría
    public List<Producto> findByIdCategoriaAndActivoTrue(Integer idCategoria);

}
