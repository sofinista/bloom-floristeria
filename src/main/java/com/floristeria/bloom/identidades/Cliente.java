package com.floristeria.bloom.identidades;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    private Integer idCliente;
    private String nombre;
    private String telefono;
    private String direccion;
    private String correoElectronico;
    private String tipoDocumento;
    private String numeroDocumento;
    private Boolean activo;
}