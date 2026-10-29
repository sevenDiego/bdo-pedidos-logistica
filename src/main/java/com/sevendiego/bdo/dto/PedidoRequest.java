package com.sevendiego.bdo.dto;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PedidoRequest(@NotNull @Valid ClienteRequest cliente, 
		@NotBlank String codigoPostalDestino,
		@NotNull @Positive BigDecimal pesoKg, @NotNull @Positive Integer numBultos, 
		@NotEmpty List<@Valid LineaPedidoRequest> lineas) {
	
}
