package com.nasu.tienda.repository;

import com.nasu.tienda.domain.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    public List<Pedido> findByIdUsuarioOrderByFechaCreacionDesc(Integer idUsuario);
}
