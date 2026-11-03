package com.floristeria.bloom.excepciones;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cuerpo de todas las respuestas de error de la API")
public record ErrorRespuesta(
        @Schema(description = "Mensaje que explica qué salió mal",
                example = "No existe un cliente activo con id 99")
        String error) {
}