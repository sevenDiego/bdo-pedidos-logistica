package com.sevendiego.bdo.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.net.http.HttpClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.sevendiego.bdo.dto.TarifaRequest;

class ProveedorLogisticoClientIntegrationTest {

    private WireMockServer wireMockServer;
    private ProveedorLogisticoClient cliente;

    @BeforeEach
    void setUp() {

        // Arrancar el servidor simulado WireMock
        wireMockServer = new WireMockServer(
                options().dynamicPort());

        wireMockServer.start();

        // Configurar el cliente HTTP para utilizar HTTP/1.1
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        // Crear el cliente del proveedor logístico
        cliente = new ProveedorLogisticoClient(
                RestClient.builder().requestFactory(
                        new JdkClientHttpRequestFactory(httpClient)),
                wireMockServer.baseUrl(),
                "token-prueba"
        );
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void debeDetectarError503DelProveedor() {

        // Simular un error del proveedor
        wireMockServer.stubFor(
                post(urlEqualTo("/api/tarifas"))
                    .willReturn(aResponse().withStatus(503))
        );

        TarifaRequest request = new TarifaRequest(
                "43840",
                new BigDecimal("3.5"),
                2
        );

        // Comprobar que el cliente detecta el error HTTP
        RestClientResponseException excepcion =
                assertThrows(
                    RestClientResponseException.class,
                    () -> cliente.calcularTarifa(request)
                );

        assertEquals(503, excepcion.getStatusCode().value());

        // Comprobar que se hizo exactamente una petición
        wireMockServer.verify(
                1,
                postRequestedFor(urlEqualTo("/api/tarifas"))
        );
    }
}
