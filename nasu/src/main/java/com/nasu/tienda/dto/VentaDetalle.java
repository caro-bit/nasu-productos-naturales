package com.nasu.tienda.dto;

import java.math.BigDecimal;

/**
 * Proyección de una línea de venta con los datos de su factura, su cliente y su
 * producto (HU-18). Los nombres de los métodos coinciden con los alias de la
 * consulta nativa de {@code VentaRepository}.
 */
public interface VentaDetalle {

    Integer getIdVenta();

    Integer getIdFactura();

    Integer getIdPedido();

    String getFecha();

    String getEstadoFactura();

    String getCliente();

    String getProducto();

    String getCategoria();

    Long getCantidad();

    BigDecimal getPrecioHistorico();

    BigDecimal getSubtotal();
}
