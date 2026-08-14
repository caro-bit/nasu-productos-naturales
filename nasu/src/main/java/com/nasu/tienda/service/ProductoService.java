package com.nasu.tienda.service;

import com.nasu.tienda.domain.Producto;
import com.nasu.tienda.repository.ProductoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

@Service
public class ProductoService {

    // El repositorio es final para asegurar la inmutabilidad
    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> getProductos(boolean activo) {
        if (activo) { //Sólo activos...
            return productoRepository.findByActivoTrue();
        }
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> getProducto(Integer idProducto) {
        return productoRepository.findById(idProducto);
    }

    @Transactional(readOnly = true)
    public List<Producto> getDestacados() {
        return productoRepository.findTop4ByActivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarProductos(String descripcion) {
        return productoRepository.findByDescripcionContainingIgnoreCaseAndActivoTrue(descripcion);
    }

    @Transactional(readOnly = true)
    public List<Producto> getProductosPorCategoria(Integer idCategoria) {
        return productoRepository.findByIdCategoriaAndActivoTrue(idCategoria);
    }

    //HU-22: valor del inventario disponible, con una consulta JPQL de agregación
    @Transactional(readOnly = true)
    public BigDecimal getValorInventario() {
        BigDecimal valor = productoRepository.calcularValorInventario();
        return valor != null ? valor : BigDecimal.ZERO;
    }

    //HU-22: cantidad de productos publicados en el catálogo
    @Transactional(readOnly = true)
    public long contarActivos() {
        return productoRepository.countByActivoTrue();
    }

    @Transactional
    public void save(Producto producto) {
        productoRepository.save(producto);
    }

    //HU-14: desactivar un producto lo saca del catálogo sin borrar su historial
    @Transactional
    public boolean cambiarEstado(Integer idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("El producto con ID " + idProducto + " no existe."));
        producto.setActivo(!producto.isActivo());
        productoRepository.save(producto);
        return producto.isActivo();
    }
    @Transactional
    public void delete(Integer idProducto) {
        if (!productoRepository.existsById(idProducto)) {
            throw new IllegalArgumentException("El producto con ID " + idProducto + " no existe.");
        }
        try {
            productoRepository.deleteById(idProducto);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("No se puede eliminar el producto.", e);
        }
    }
    
}
