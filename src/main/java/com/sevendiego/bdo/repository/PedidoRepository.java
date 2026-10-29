package com.sevendiego.bdo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sevendiego.bdo.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

}
