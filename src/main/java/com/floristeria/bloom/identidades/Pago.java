package com.floristeria.bloom.identidades;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    private Integer idPago;
    private Integer idPedido;
    private Double monto;
    private String metodo;
    private LocalDateTime fechaPago;
}