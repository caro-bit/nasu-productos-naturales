package com.nasu.tienda.repository;

import com.nasu.tienda.domain.DetPedido;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetPedidoRepository extends JpaRepository<DetPedido, Integer> {

    @EntityGraph(attributePaths = "producto")
    public List<DetPedido> findByIdPedido(Integer idPedido);
}