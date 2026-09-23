package com.floristeria.bloom.identidades;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    private Integer idPedido;
    private Integer idCliente;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaHoraEntrega;
    private String direccionEntrega;
    private String ocasion;
    private String estado;
    private Double valorTotal;
    private LocalDateTime fechaEntregaReal;
    private List<Arreglo> arreglos;
}