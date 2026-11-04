package com.floristeria.bloom.identidades;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pago")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Abono realizado a un pedido")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpago")
    @Schema(description = "Identificador generado por el sistema", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer idPago;

    @Column(name = "idpedido", nullable = false)
    @Schema(description = "Id del pedido que se está abonando", example = "1")
    private Integer idPedido;

    @Column(name = "monto", nullable = false)
    @Schema(description = "Valor del abono; mayor a 0 y sin superar el saldo", example = "50000")
    private Double monto;

    @Column(name = "metodo", nullable = false)
    @Schema(description = "EFECTIVO, TARJETA o TRANSFERENCIA", example = "EFECTIVO")
    private String metodo;

    @Column(name = "fechapago", updatable = false)
    @Schema(description = "Fecha y hora del pago (la asigna el sistema)", example = "2026-11-04T09:45:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaPago;

    @PrePersist
    protected void antesDeGuardar() {
        fechaPago = LocalDateTime.now();
    }
}