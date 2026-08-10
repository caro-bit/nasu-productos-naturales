package com.nasu.tienda.repository;

import com.nasu.tienda.domain.Venta;
import com.nasu.tienda.dto.ResumenVentas;
import com.nasu.tienda.dto.VentaDetalle;
import com.nasu.tienda.dto.VentaPorDia;
import com.nasu.tienda.dto.VentaPorProducto;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    @EntityGraph(attributePaths = "producto")
    public List<Venta> findByIdFactura(Integer idFactura);

    //HU-18: detalle de las ventas registradas en un rango de fechas.
    //Las facturas anuladas se excluyen porque no representan ventas efectivas.
    @Query(nativeQuery = true, value = """
            SELECT v.id_venta                                 AS idVenta,
                   f.id_factura                               AS idFactura,
                   f.id_pedido                                AS idPedido,
                   DATE_FORMAT(f.fecha_creacion, '%d/%m/%Y %H:%i') AS fecha,
                   f.estado                                   AS estadoFactura,
                   CONCAT(u.nombre, ' ', u.apellidos)         AS cliente,
                   p.descripcion                              AS producto,
                   c.descripcion                              AS categoria,
                   v.cantidad                                 AS cantidad,
                   v.precio_historico                         AS precioHistorico,
                   (v.cantidad * v.precio_historico)          AS subtotal
            FROM venta v
                 JOIN factura   f ON f.id_factura   = v.id_factura
                 JOIN usuario   u ON u.id_usuario   = f.id_usuario
                 JOIN producto  p ON p.id_producto  = v.id_producto
                 JOIN categoria c ON c.id_categoria = p.id_categoria
            WHERE f.estado <> 'Anulada'
              AND f.fecha_creacion BETWEEN :inicio AND :fin
            ORDER BY f.fecha_creacion DESC, v.id_venta DESC
            """)
    public List<VentaDetalle> findDetalleVentas(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    //HU-18 y HU-19: totales generales del período (facturas, unidades y monto)
    @Query(nativeQuery = true, value = """
            SELECT COUNT(DISTINCT f.id_factura)                            AS facturas,
                   COALESCE(CAST(SUM(v.cantidad) AS SIGNED), 0)            AS unidades,
                   COALESCE(SUM(v.cantidad * v.precio_historico), 0)       AS monto
            FROM venta v
                 JOIN factura f ON f.id_factura = v.id_factura
            WHERE f.estado <> 'Anulada'
              AND f.fecha_creacion BETWEEN :inicio AND :fin
            """)
    public ResumenVentas findResumenVentas(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    //HU-19: ventas agrupadas por producto, de mayor a menor monto vendido
    @Query(nativeQuery = true, value = """
            SELECT p.id_producto                      AS idProducto,
                   p.descripcion                      AS producto,
                   c.descripcion                      AS categoria,
                   CAST(SUM(v.cantidad) AS SIGNED)    AS unidades,
                   SUM(v.cantidad * v.precio_historico) AS monto
            FROM venta v
                 JOIN factura   f ON f.id_factura   = v.id_factura
                 JOIN producto  p ON p.id_producto  = v.id_producto
                 JOIN categoria c ON c.id_categoria = p.id_categoria
            WHERE f.estado <> 'Anulada'
              AND f.fecha_creacion BETWEEN :inicio AND :fin
            GROUP BY p.id_producto, p.descripcion, c.descripcion
            ORDER BY monto DESC
            """)
    public List<VentaPorProducto> findVentasPorProducto(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    //HU-19: ventas agrupadas por día para observar el comportamiento del período
    @Query(nativeQuery = true, value = """
            SELECT DATE_FORMAT(f.fecha_creacion, '%d/%m/%Y')  AS fecha,
                   COUNT(DISTINCT f.id_factura)               AS facturas,
                   CAST(SUM(v.cantidad) AS SIGNED)            AS unidades,
                   SUM(v.cantidad * v.precio_historico)       AS monto
            FROM venta v
                 JOIN factura f ON f.id_factura = v.id_factura
            WHERE f.estado <> 'Anulada'
              AND f.fecha_creacion BETWEEN :inicio AND :fin
            GROUP BY DATE_FORMAT(f.fecha_creacion, '%d/%m/%Y')
            ORDER BY MIN(f.fecha_creacion)
            """)
    public List<VentaPorDia> findVentasPorDia(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}
