package com.sevendiego.bdo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.sevendiego.bdo.dto.TarifaRequest;
import com.sevendiego.bdo.dto.TarifaResponse;

@Component
public class ProveedorLogisticoClient {

    private final RestClient restClient;
    private final String token;

    public ProveedorLogisticoClient(
            RestClient.Builder builder,
            @Value("${logistica.base-url}") String baseUrl,
            @Value("${logistica.token}") String token) {

        this.restClient = builder.baseUrl(baseUrl).build();
        this.token = token;
    }

    public TarifaResponse calcularTarifa(TarifaRequest request) {

        TarifaResponse respuesta = restClient.post()
                .uri("/api/tarifas")
                .headers(headers -> headers.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TarifaResponse.class);

        if (respuesta == null) {
            throw new IllegalStateException(
                    "El proveedor no ha devuelto una tarifa.");
        }

        return respuesta;
    }
}