package com.nasu.tienda.service;

import com.nasu.tienda.domain.Pedido;
import com.nasu.tienda.repository.PedidoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<Pedido> getPedidosPorUsuario(Integer idUsuario) {
        return pedidoRepository.findByIdUsuarioOrderByFechaCreacionDesc(idUsuario);
    }
}
