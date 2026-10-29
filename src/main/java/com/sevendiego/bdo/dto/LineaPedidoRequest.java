package com.sevendiego.bdo.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LineaPedidoRequest(@NotBlank String producto, 
		@NotNull @Positive Integer cantidad,
		@NotNull @DecimalMin("0.01") BigDecimal precioUnitario) {
	
}