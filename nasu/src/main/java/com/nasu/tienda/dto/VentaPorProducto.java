package com.nasu.tienda.dto;

import java.math.BigDecimal;

/**
 * Proyección con las unidades y el monto vendido de cada producto dentro de un
 * período (HU-19).
 */
public interface VentaPorProducto {

    Integer getIdProducto();

    String getProducto();

    String getCategoria();

    Long getUnidades();

    BigDecimal getMonto();
}
