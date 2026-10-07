package com.floristeria.bloom.identidades;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flor {
    private Integer idFlor;
    private String nombre;
    private String color;
    private Integer stock;
    private Double precioUnitario;
    private Boolean activo;
}