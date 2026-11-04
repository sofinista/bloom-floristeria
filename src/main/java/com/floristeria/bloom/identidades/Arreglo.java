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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "arreglo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Arreglo floral que forma parte de un pedido")
public class Arreglo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idarreglo")
    @Schema(description = "Identificador generado por el sistema", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer idArreglo;

    // ManyToOne: muchos arreglos pertenecen a un pedido. Este es el lado dueño (tiene la llave foránea).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idpedido", nullable = false)
    @JsonIgnore
    private Pedido pedido;

    @Column(name = "tipoarreglo", nullable = false)
    @Schema(description = "Tipo de arreglo", example = "Ramo")
    private String tipoArreglo;

    @Column(name = "tamano")
    @Schema(description = "Tamaño", example = "Grande")
    private String tamano;

    @Column(name = "colores")
    @Schema(description = "Colores predominantes", example = "Rojo y blanco")
    private String colores;

    @Column(name = "descripcion")
    @Schema(description = "Detalles del arreglo", example = "Rosas y lirios con follaje verde")
    private String descripcion;

    @Column(name = "valorunitario", nullable = false)
    @Schema(description = "Valor del arreglo; debe ser mayor a 0", example = "85000")
    private Double valorUnitario;
}