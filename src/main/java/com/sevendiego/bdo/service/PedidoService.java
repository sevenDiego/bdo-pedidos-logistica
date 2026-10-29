package com.sevendiego.bdo.service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sevendiego.bdo.dto.ClienteRequest;
import com.sevendiego.bdo.dto.LineaPedidoRequest;
import com.sevendiego.bdo.dto.PedidoRequest;
import com.sevendiego.bdo.model.Cliente;
import com.sevendiego.bdo.model.LineaPedido;
import com.sevendiego.bdo.model.Pedido;
import com.sevendiego.bdo.repository.ClienteRepository;
import com.sevendiego.bdo.repository.PedidoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ClienteRepository clienteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Long crearPedido(PedidoRequest request) {
        // Buscar cliente existente o crear uno nuevo
    	Optional<Cliente> clienteEncontrado = buscarClientePorEmail(request.cliente().email());

    	Cliente cliente;
        if (clienteEncontrado.isPresent()) {
            cliente = clienteEncontrado.get();
        } else {
            cliente = crearCliente(request.cliente());
        }
        
        // Crear pedido
        Pedido pedido = construirPedido(request, cliente);

        // Añadir líneas y calcular importe
        agregarLineasYCalcularImportes(pedido, request.lineas());

        // Guardar pedido y líneas
        return pedidoRepository.save(pedido).getId();
    }

    private Optional<Cliente> buscarClientePorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }
    
    private Cliente crearCliente(ClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setNombre(request.nombre());
        cliente.setEmail(request.email());

        return clienteRepository.save(cliente);
    }
    
    private Pedido construirPedido(PedidoRequest request,
            Cliente cliente) {
		Pedido pedido = new Pedido();
		pedido.setCliente(cliente);
		pedido.setCodigoPostalDestino(request.codigoPostalDestino());
		pedido.setPesoKg(request.pesoKg());
		pedido.setFecha(LocalDateTime.now(ZoneOffset.UTC));
		pedido.setNumeroBultos(request.numBultos());
		
		return pedido;
    }
    
    private void agregarLineasYCalcularImportes(Pedido pedido, List<LineaPedidoRequest> lineas) {

        BigDecimal importeProductos = BigDecimal.ZERO;

        for (LineaPedidoRequest dato : lineas) {

            LineaPedido linea = new LineaPedido();
            linea.setProducto(dato.producto());
            linea.setCantidad(dato.cantidad());
            linea.setPrecioUnitario(dato.precioUnitario());

            pedido.addLinea(linea);

            importeProductos = importeProductos.add(linea.calcularSubtotal());
        }

        pedido.setImporteProductos(importeProductos);
        pedido.setCosteEnvio(BigDecimal.ZERO);
        pedido.setImporteTotal(importeProductos);
    }
}
