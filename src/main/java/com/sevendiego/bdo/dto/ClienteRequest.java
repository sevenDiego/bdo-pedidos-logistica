package com.sevendiego.bdo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(@NotBlank String nombre, @NotBlank @Email String email) {
	
}