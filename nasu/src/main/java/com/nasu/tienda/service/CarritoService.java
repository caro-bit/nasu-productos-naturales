package com.nasu.tienda.service;

import com.nasu.tienda.domain.Carrito;
import com.nasu.tienda.domain.DetCarrito;
import com.nasu.tienda.domain.Producto;
import com.nasu.tienda.repository.CarritoRepository;
import com.nasu.tienda.repository.DetCarritoRepository;
import com.nasu.tienda.repository.ProductoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final DetCarritoRepository detCarritoRepository;
    private final ProductoRepository productoRepository;

    public CarritoService(CarritoRepository carritoRepository, DetCarritoRepository detCarritoRepository,
            ProductoRepository productoRepository) {
        this.carritoRepository = carritoRepository;
        this.detCarritoRepository = detCarritoRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public Carrito getCarritoActivo(Integer idUsuario) {
        return carritoRepository.findByIdUsuarioAndActivoTrue(idUsuario).orElseGet(() -> {
            Carrito carrito = new Carrito();
            carrito.setIdUsuario(idUsuario);
            carrito.setActivo(Boolean.TRUE);
            return carritoRepository.save(carrito);
        });
    }

    @Transactional(readOnly = true)
    public List<DetCarrito> getDetalleActivo(Integer idUsuario) {
        var carritoOpt = carritoRepository.findByIdUsuarioAndActivoTrue(idUsuario);
        if (carritoOpt.isEmpty()) {
            return List.of();
        }
        return detCarritoRepository.findByIdCarritoAndActivoTrue(carritoOpt.get().getIdCarrito());
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotal(List<DetCarrito> detalles) {
        return detalles.stream()
                .map(DetCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void agregarProducto(Integer idUsuario, Integer idProducto, Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        }

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        if (!producto.isActivo() || producto.getExistencias() <= 0) {
            throw new IllegalArgumentException("El producto no está disponible.");
        }

        var carrito = getCarritoActivo(idUsuario);
        var detalleOpt = detCarritoRepository.findByIdCarritoAndIdProductoAndActivoTrue(
                carrito.getIdCarrito(), idProducto);

        int cantidadActual = detalleOpt.map(DetCarrito::getCantidad).orElse(0);
        int nuevaCantidad = cantidadActual + cantidad;
        if (nuevaCantidad > producto.getExistencias()) {
            throw new IllegalArgumentException("No hay existencias suficientes para agregar esa cantidad.");
        }

        DetCarrito detalle = detalleOpt.orElseGet(DetCarrito::new);
        detalle.setIdUsuario(idUsuario);
        detalle.setIdCarrito(carrito.getIdCarrito());
        detalle.setIdProducto(idProducto);
        detalle.setCantidad(nuevaCantidad);
        detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(nuevaCantidad)));
        detalle.setActivo(Boolean.TRUE);
        detCarritoRepository.save(detalle);
    }

    @Transactional
    public void actualizarCantidad(Integer idUsuario, Integer idDetcarrito, Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        }

        DetCarrito detalle = detCarritoRepository.findById(idDetcarrito)
                .orElseThrow(() -> new IllegalArgumentException("El producto no existe en el carrito."));

        if (!detalle.getIdUsuario().equals(idUsuario) || !Boolean.TRUE.equals(detalle.getActivo())) {
            throw new IllegalArgumentException("No se puede modificar este producto del carrito.");
        }

        Producto producto = productoRepository.findById(detalle.getIdProducto())
                .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        if (cantidad > producto.getExistencias()) {
            throw new IllegalArgumentException("No hay existencias suficientes para esa cantidad.");
        }

        detalle.setCantidad(cantidad);
        detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(cantidad)));
        detCarritoRepository.save(detalle);
    }

    @Transactional
    public void eliminarProducto(Integer idUsuario, Integer idDetcarrito) {
        DetCarrito detalle = detCarritoRepository.findById(idDetcarrito)
                .orElseThrow(() -> new IllegalArgumentException("El producto no existe en el carrito."));

        if (!detalle.getIdUsuario().equals(idUsuario)) {
            throw new IllegalArgumentException("No se puede eliminar este producto del carrito.");
        }

        detalle.setActivo(Boolean.FALSE);
        detCarritoRepository.save(detalle);
    }

    @Transactional
    public void vaciarCarrito(Integer idUsuario) {
        var detalles = getDetalleActivo(idUsuario);
        detalles.forEach(d -> d.setActivo(Boolean.FALSE));
        detCarritoRepository.saveAll(detalles);
    }
}
