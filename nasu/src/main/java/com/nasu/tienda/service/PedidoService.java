package com.nasu.tienda.service;

import com.nasu.tienda.domain.DetCarrito;
import com.nasu.tienda.domain.DetPedido;
import com.nasu.tienda.domain.Factura;
import com.nasu.tienda.domain.Pedido;
import com.nasu.tienda.domain.Producto;
import com.nasu.tienda.domain.Venta;
import com.nasu.tienda.repository.DetPedidoRepository;
import com.nasu.tienda.repository.FacturaRepository;
import com.nasu.tienda.repository.PedidoRepository;
import com.nasu.tienda.repository.ProductoRepository;
import com.nasu.tienda.repository.VentaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetPedidoRepository detPedidoRepository;
    private final FacturaRepository facturaRepository;
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final CarritoService carritoService;

    public PedidoService(PedidoRepository pedidoRepository, DetPedidoRepository detPedidoRepository,
            FacturaRepository facturaRepository, VentaRepository ventaRepository,
            ProductoRepository productoRepository, CarritoService carritoService) {
        this.pedidoRepository = pedidoRepository;
        this.detPedidoRepository = detPedidoRepository;
        this.facturaRepository = facturaRepository;
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.carritoService = carritoService;
    }

    @Transactional(readOnly = true)
    public List<Pedido> getPedidosPorUsuario(Integer idUsuario) {
        return pedidoRepository.findByIdUsuarioOrderByFechaCreacionDesc(idUsuario);
    }
    
    @Transactional(readOnly = true)
    public Optional<Pedido> getPedido(Integer idUsuario, Integer idPedido) {
        return pedidoRepository.findByIdPedidoAndIdUsuario(idPedido, idUsuario);
    }
    
    @Transactional(readOnly = true)
    public List<DetPedido> getDetallePedido(Integer idPedido) {
        return detPedidoRepository.findByIdPedido(idPedido);
    }

    @Transactional
    public Pedido confirmarPedido(Integer idUsuario, Integer idDireccion, Integer idMetodoPago) {
        var detalles = carritoService.getDetalleActivo(idUsuario);
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío.");
        }

        for (DetCarrito detalle : detalles) {
            Producto producto = productoRepository.findById(detalle.getIdProducto())
                    .orElseThrow(() -> new IllegalArgumentException("El producto ya no existe."));
            if (detalle.getCantidad() > producto.getExistencias()) {
                throw new IllegalArgumentException(
                        "No hay existencias suficientes de " + producto.getDescripcion() + ".");
            }
        }

        BigDecimal total = carritoService.calcularTotal(detalles);

        Pedido pedido = new Pedido();
        pedido.setIdUsuario(idUsuario);
        pedido.setIdDireccion(idDireccion);
        pedido.setEstado("Pendiente");
        pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        for (DetCarrito detalle : detalles) {
            Producto producto = productoRepository.findById(detalle.getIdProducto()).get();

            DetPedido detPedido = new DetPedido();
            detPedido.setIdPedido(pedido.getIdPedido());
            detPedido.setIdProducto(detalle.getIdProducto());
            detPedido.setCantidad(detalle.getCantidad());
            detPedido.setPrecioUnitario(producto.getPrecio());
            detPedidoRepository.save(detPedido);

            producto.setExistencias(producto.getExistencias() - detalle.getCantidad());
            productoRepository.save(producto);
        }

        Factura factura = new Factura();
        factura.setIdUsuario(idUsuario);
        factura.setIdPedido(pedido.getIdPedido());
        factura.setIdMetodoPago(idMetodoPago);
        factura.setTotal(total);
        factura.setEstado("Pagada");
        factura.setReferenciaTransaccion(generarReferenciaSimulada());
        factura = facturaRepository.save(factura);

        for (DetCarrito detalle : detalles) {
            Producto producto = productoRepository.findById(detalle.getIdProducto()).get();

            Venta venta = new Venta();
            venta.setIdFactura(factura.getIdFactura());
            venta.setIdProducto(detalle.getIdProducto());
            venta.setPrecioHistorico(producto.getPrecio());
            venta.setCantidad(detalle.getCantidad());
            ventaRepository.save(venta);
        }

        carritoService.vaciarCarrito(idUsuario);
        return pedido;
    }

    private String generarReferenciaSimulada() {
        return "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}