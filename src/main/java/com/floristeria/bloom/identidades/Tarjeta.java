package com.floristeria.bloom.identidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tarjeta")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tarjeta con dedicatoria que acompaña un pedido (máximo una por pedido)")
public class Tarjeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtarjeta")
    @Schema(description = "Identificador generado por el sistema", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer idTarjeta;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idpedido", nullable = false, unique = true)
    @JsonIgnore
    private Pedido pedido;

    @Column(name = "mensaje", nullable = false, length = 200)
    @Schema(description = "Dedicatoria (máximo 200 caracteres)", example = "Feliz cumpleaños, te quiero mucho")
    private String mensaje;

    @Column(name = "remitente", length = 80)
    @Schema(description = "Quién firma la tarjeta (opcional)", example = "Carlos")
    private String remitente;
}