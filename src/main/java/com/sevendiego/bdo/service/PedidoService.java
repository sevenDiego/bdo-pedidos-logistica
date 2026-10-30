package com.sevendiego.bdo.service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.sevendiego.bdo.client.ProveedorLogisticoClient;
import com.sevendiego.bdo.dto.ClienteRequest;
import com.sevendiego.bdo.dto.LineaPedidoRequest;
import com.sevendiego.bdo.dto.PedidoRequest;
import com.sevendiego.bdo.dto.TarifaRequest;
import com.sevendiego.bdo.dto.TarifaResponse;
import com.sevendiego.bdo.model.Cliente;
import com.sevendiego.bdo.model.EstadoPedido;
import com.sevendiego.bdo.model.LineaPedido;
import com.sevendiego.bdo.model.Pedido;
import com.sevendiego.bdo.repository.ClienteRepository;
import com.sevendiego.bdo.repository.PedidoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProveedorLogisticoClient proveedorLogisticoClient;

    public PedidoService(PedidoRepository pedidoRepository,
                         ClienteRepository clienteRepository,
                         ProveedorLogisticoClient proveedorLogisticoClient) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.proveedorLogisticoClient = proveedorLogisticoClient;
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
    
    public void confirmarPedido(Long id) {

        // Reservar el pedido para esta confirmación
        int actualizados = pedidoRepository.actualizarEstadoSiCoincide(
                id,
                EstadoPedido.PENDIENTE,
                EstadoPedido.CALCULANDO_ENVIO
        );

        if (actualizados == 0) {
            if (!pedidoRepository.existsById(id)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido no encontrado.");
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El pedido no está pendiente.");
        }

        // Recuperar pedido
        Pedido pedido = pedidoRepository.findById(id).orElseThrow();

        // Consultar proveedor logístico
        TarifaResponse tarifa;

        try {
            TarifaRequest request = new TarifaRequest(
                    pedido.getCodigoPostalDestino(),
                    pedido.getPesoKg(),
                    pedido.getNumeroBultos()
            );

            tarifa = proveedorLogisticoClient.calcularTarifa(request);
            validarTarifa(tarifa);

        } catch (RestClientException | IllegalStateException e) {

            pedidoRepository.actualizarEstadoSiCoincide(
                    id,
                    EstadoPedido.CALCULANDO_ENVIO,
                    EstadoPedido.ERROR_ENVIO
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Error al consultar el proveedor logístico.", e);
        }

        // Actualizar importe y confirmar pedido
        pedido.setCosteEnvio(tarifa.coste());
        pedido.setImporteTotal(
                pedido.getImporteProductos().add(tarifa.coste())
        );
        pedido.setEstado(EstadoPedido.CONFIRMADO);

        pedidoRepository.save(pedido);
    }

	private void validarTarifa(TarifaResponse tarifa) {
	    if (tarifa == null
	            || tarifa.coste() == null
	            || tarifa.coste().signum() < 0
	            || tarifa.coste().scale() > 2
	            || !"EUR".equals(tarifa.moneda())) {

	        throw new IllegalStateException(
	                "Tarifa del proveedor no válida.");
	    }		
	}
}
