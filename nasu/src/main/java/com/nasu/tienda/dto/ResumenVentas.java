package com.nasu.tienda.dto;

import java.math.BigDecimal;

/**
 * Proyección con los totales generales del período consultado: facturas
 * emitidas, unidades vendidas y monto acumulado (HU-18 y HU-19).
 */
public interface ResumenVentas {

    Long getFacturas();

    Long getUnidades();

    BigDecimal getMonto();
}
