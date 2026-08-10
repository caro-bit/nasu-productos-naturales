package com.nasu.tienda.dto;

import java.math.BigDecimal;

/**
 * Proyección con el total vendido en cada día del período consultado (HU-19).
 */
public interface VentaPorDia {

    String getFecha();

    Long getFacturas();

    Long getUnidades();

    BigDecimal getMonto();
}
