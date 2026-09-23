package com.floristeria.bloom.identidades;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Arreglo {

    private Integer idArreglo;
    private Integer idPedido;
    private String tipoArreglo;
    private String tamano;
    private String colores;
    private String descripcion;
    private Double valorUnitario;
}