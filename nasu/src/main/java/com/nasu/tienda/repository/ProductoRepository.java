package com.nasu.tienda.repository;

import com.nasu.tienda.domain.Producto;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    //HU-17: consulta derivada para los productos activos cuyo inventario llegó al umbral
    public List<Producto> findByActivoTrueAndExistenciasLessThanEqualOrderByExistenciasAsc(Integer umbral);

    //HU-22: consulta JPQL que valoriza el inventario disponible (precio por existencias)
    @Query("SELECT SUM(p.precio * p.existencias) FROM Producto p WHERE p.activo = true")
    public BigDecimal calcularValorInventario();

    //HU-22: cantidad de productos publicados en el catálogo
    public long countByActivoTrue();

}
