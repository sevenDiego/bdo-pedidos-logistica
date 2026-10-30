package com.sevendiego.bdo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sevendiego.bdo.client.ProveedorLogisticoClient;
import com.sevendiego.bdo.dto.ClienteRequest;
import com.sevendiego.bdo.dto.LineaPedidoRequest;
import com.sevendiego.bdo.dto.PedidoRequest;
import com.sevendiego.bdo.model.Cliente;
import com.sevendiego.bdo.model.Pedido;
import com.sevendiego.bdo.repository.ClienteRepository;
import com.sevendiego.bdo.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProveedorLogisticoClient proveedorLogisticoClient;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void debeCalcularCorrectamenteElTotalDelPedido() {

        // Preparar los datos
        Cliente cliente = new Cliente();
        cliente.setNombre("Juan");
        cliente.setEmail("juan@example.com");

        PedidoRequest request = new PedidoRequest(
                new ClienteRequest("Juan", "juan@example.com"),
                "43840",
                new BigDecimal("3.5"),
                2,
                List.of(
                    new LineaPedidoRequest(
                            "Teclado", 2, new BigDecimal("25.00")),
                    new LineaPedidoRequest(
                            "Raton", 3, new BigDecimal("10.00"))
                )
        );

        // Simular la búsqueda del cliente
        when(clienteRepository.findByEmail("juan@example.com"))
                .thenReturn(Optional.of(cliente));

        // Simular el guardado del pedido
        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocacion -> {
                    Pedido pedido = invocacion.getArgument(0);
                    pedido.setId(1L);
                    return pedido;
                });

        // Ejecutar el método que queremos probar
        Long id = pedidoService.crearPedido(request);

        // Recuperar el pedido enviado al repositorio
        ArgumentCaptor<Pedido> captor =
                ArgumentCaptor.forClass(Pedido.class);

        verify(pedidoRepository).save(captor.capture());

        Pedido pedido = captor.getValue();

        // Comprobar los resultados
        assertEquals(1L, id);

        assertEquals(0,
                new BigDecimal("80.00")
                        .compareTo(pedido.getImporteProductos()));

        assertEquals(0,
                new BigDecimal("80.00")
                        .compareTo(pedido.getImporteTotal()));

        assertEquals(2, pedido.getLineas().size());
    }
}
