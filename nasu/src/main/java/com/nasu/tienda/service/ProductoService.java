package com.nasu.tienda.service;

import com.nasu.tienda.domain.Producto;
import com.nasu.tienda.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<Producto> getDestacados() {
        return productoRepository.findTop4ByActivoTrue();
    }

}
