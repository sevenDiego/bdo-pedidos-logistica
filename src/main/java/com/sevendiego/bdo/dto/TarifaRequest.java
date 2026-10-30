package com.sevendiego.bdo.dto;

import java.math.BigDecimal;

public record TarifaRequest(String codigoPostalDestino, BigDecimal pesoKg, Integer numeroBultos) {
	
}
