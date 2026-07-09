package com.nasu.tienda.service;

import com.nasu.tienda.domain.Direccion;
import com.nasu.tienda.repository.DireccionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DireccionService {

    private final DireccionRepository direccionRepository;

    public DireccionService(DireccionRepository direccionRepository) {
        this.direccionRepository = direccionRepository;
    }

    @Transactional(readOnly = true)
    public List<Direccion> getDireccionesPorUsuario(Integer idUsuario) {
        return direccionRepository.findByIdUsuario(idUsuario);
    }

    @Transactional
    public Direccion guardar(Direccion direccion) {
        return direccionRepository.save(direccion);
    }
}