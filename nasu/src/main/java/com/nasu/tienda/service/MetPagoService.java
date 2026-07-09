package com.nasu.tienda.service;

import com.nasu.tienda.domain.MetPago;
import com.nasu.tienda.repository.MetPagoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetPagoService {

    private final MetPagoRepository metPagoRepository;

    public MetPagoService(MetPagoRepository metPagoRepository) {
        this.metPagoRepository = metPagoRepository;
    }

    @Transactional(readOnly = true)
    public List<MetPago> getMetodosPago() {
        return metPagoRepository.findAll();
    }
}