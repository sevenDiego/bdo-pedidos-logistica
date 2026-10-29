package com.sevendiego.bdo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.sevendiego.bdo.dto.PedidoRequest;
import com.sevendiego.bdo.service.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<Long> crearPedido(@Valid @RequestBody PedidoRequest request) {

        Long id = pedidoService.crearPedido(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(id);
    }
}