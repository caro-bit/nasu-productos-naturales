package com.nasu.tienda.service;

import com.nasu.tienda.domain.Producto;
import com.nasu.tienda.dto.ResumenVentas;
import com.nasu.tienda.dto.VentaDetalle;
import com.nasu.tienda.dto.VentaPorDia;
import com.nasu.tienda.dto.VentaPorProducto;
import com.nasu.tienda.repository.ProductoRepository;
import com.nasu.tienda.repository.VentaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consultas de apoyo para la administración del negocio: control de inventario
 * (HU-17), consulta de ventas (HU-18) y reportes por período (HU-19).
 */
@Service
public class ReporteService {

    //Cantidad de existencias a partir de la cual un producto se considera bajo
    public static final int UMBRAL_BAJO_INVENTARIO = 10;

    //Fecha de arranque del negocio, se usa cuando el administrador no filtra por fecha
    public static final LocalDate FECHA_MINIMA = LocalDate.of(2000, 1, 1);

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    public ReporteService(ProductoRepository productoRepository, VentaRepository ventaRepository) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
    }

    // --- HU-17: productos con bajo inventario ---

    @Transactional(readOnly = true)
    public List<Producto> getProductosBajoInventario(int umbral) {
        return productoRepository.findByActivoTrueAndExistenciasLessThanEqualOrderByExistenciasAsc(umbral);
    }

    //Los productos sin existencias son los que deben reabastecerse primero
    public long contarAgotados(List<Producto> productos) {
        return productos.stream()
                .filter(p -> p.getExistencias() != null && p.getExistencias() == 0)
                .count();
    }

    //Un umbral vacío o negativo no tiene sentido para el control de inventario
    public int resolverUmbral(Integer umbral) {
        if (umbral == null || umbral < 0) {
            return UMBRAL_BAJO_INVENTARIO;
        }
        return umbral;
    }

    // --- HU-18 y HU-19: ventas ---

    @Transactional(readOnly = true)
    public List<VentaDetalle> getVentas(LocalDate desde, LocalDate hasta) {
        return ventaRepository.findDetalleVentas(inicioDelDia(desde), finDelDia(hasta));
    }

    @Transactional(readOnly = true)
    public ResumenVentas getResumen(LocalDate desde, LocalDate hasta) {
        return ventaRepository.findResumenVentas(inicioDelDia(desde), finDelDia(hasta));
    }

    @Transactional(readOnly = true)
    public List<VentaPorProducto> getVentasPorProducto(LocalDate desde, LocalDate hasta) {
        return ventaRepository.findVentasPorProducto(inicioDelDia(desde), finDelDia(hasta));
    }

    @Transactional(readOnly = true)
    public List<VentaPorDia> getVentasPorDia(LocalDate desde, LocalDate hasta) {
        return ventaRepository.findVentasPorDia(inicioDelDia(desde), finDelDia(hasta));
    }

    //HU-22: productos más vendidos de todo el histórico, para el panel
    @Transactional(readOnly = true)
    public List<VentaPorProducto> getTopProductos(int limite) {
        return getVentasPorProducto(FECHA_MINIMA, LocalDate.now()).stream()
                .limit(limite)
                .toList();
    }

    //Monto promedio por factura, útil para comparar períodos entre sí
    public BigDecimal calcularTicketPromedio(ResumenVentas resumen) {
        if (resumen == null || resumen.getFacturas() == null || resumen.getFacturas() == 0) {
            return BigDecimal.ZERO;
        }
        return resumen.getMonto().divide(BigDecimal.valueOf(resumen.getFacturas()), 2, RoundingMode.HALF_UP);
    }

    //Monto del mejor día del período, sirve de referencia para comparar los demás
    public double calcularMontoMaximo(List<VentaPorDia> ventasPorDia) {
        return ventasPorDia.stream()
                .filter(v -> v.getMonto() != null)
                .mapToDouble(v -> v.getMonto().doubleValue())
                .max()
                .orElse(0d);
    }

    //Sin fecha inicial la consulta abarca todo el histórico de ventas
    public LocalDate resolverDesde(LocalDate desde) {
        return desde != null ? desde : FECHA_MINIMA;
    }

    //Sin fecha final la consulta llega hasta el día de hoy
    public LocalDate resolverHasta(LocalDate hasta) {
        return hasta != null ? hasta : LocalDate.now();
    }

    private LocalDateTime inicioDelDia(LocalDate fecha) {
        return fecha.atStartOfDay();
    }

    //El día final se incluye completo para no dejar por fuera las ventas de esa fecha
    private LocalDateTime finDelDia(LocalDate fecha) {
        return fecha.atTime(LocalTime.MAX);
    }
}
