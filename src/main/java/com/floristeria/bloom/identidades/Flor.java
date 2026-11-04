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
@Schema(description = "Flor del inventario")
public class Flor {

    @Schema(description = "Identificador generado por el sistema", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer idFlor;

    @Schema(description = "Nombre de la flor (obligatorio)", example = "Rosa")
    private String nombre;

    @Schema(description = "Color (obligatorio)", example = "Roja")
    private String color;

    @Schema(description = "Unidades en inventario; 0 o mayor al crear (después se ajusta con /flores/stock)",
            example = "50")
    private Integer stock;

    @Schema(description = "Precio por unidad; debe ser mayor a 0", example = "2500")
    private Double precioUnitario;

    @Schema(description = "Indica si la flor está activa (lo maneja el sistema)", example = "true",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean activo;
}