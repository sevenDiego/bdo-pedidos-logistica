package com.sevendiego.bdo.dto;

import java.math.BigDecimal;

public record TarifaResponse (BigDecimal coste, String moneda, Integer plazoDias) {

}
