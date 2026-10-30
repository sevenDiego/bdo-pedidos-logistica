package com.sevendiego.bdo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.sevendiego.bdo.model.EstadoPedido;
import com.sevendiego.bdo.model.Pedido;


public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	@Modifying
	@Transactional
	@Query("""
	    UPDATE Pedido p
	    SET p.estado = :nuevoEstado
	    WHERE p.id = :id
	    AND p.estado = :estadoActual
	    """)
	int actualizarEstadoSiCoincide(
			@Param("id") Long id,
	        @Param("estadoActual") EstadoPedido estadoActual,
	        @Param("nuevoEstado") EstadoPedido nuevoEstado
	);
}
