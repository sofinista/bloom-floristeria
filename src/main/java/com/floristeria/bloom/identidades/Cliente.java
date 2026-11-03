package com.floristeria.bloom.identidades;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cliente de la floristería")
public class Cliente {

    @Schema(description = "Identificador generado por el sistema", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer idCliente;

    @Schema(description = "Nombre completo (obligatorio)", example = "Ana Gómez")
    private String nombre;

    @Schema(description = "Teléfono de contacto (obligatorio)", example = "3001234567")
    private String telefono;

    @Schema(description = "Dirección del cliente (opcional)", example = "Calle 10 # 20-30, El Poblado")
    private String direccion;

    @Schema(description = "Correo electrónico (opcional)", example = "ana@correo.com")
    private String correoElectronico;

    @Schema(description = "Tipo de documento (obligatorio): CC, CE o PASAPORTE", example = "CC")
    private String tipoDocumento;

    @Schema(description = "Número de documento (obligatorio, único)", example = "1037123456")
    private String numeroDocumento;

    @Schema(description = "Indica si el cliente está activo (lo maneja el sistema)", example = "true",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean activo;
}