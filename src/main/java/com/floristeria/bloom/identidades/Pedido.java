package com.floristeria.bloom.identidades;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pedido de arreglos florales de un cliente")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpedido")
    @Schema(description = "Identificador generado por el sistema", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer idPedido;

    @Column(name = "idcliente", nullable = false)
    @Schema(description = "Id de un cliente existente y activo", example = "1")
    private Integer idCliente;

    @Column(name = "fecharegistro", updatable = false)
    @Schema(description = "Fecha y hora en que se registró el pedido (la asigna el sistema)",
            example = "2026-11-04T09:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaRegistro;

    @Column(name = "fechahoraentrega", nullable = false)
    @Schema(description = "Fecha y hora de entrega; debe ser futura", example = "2026-12-24T10:00:00")
    private LocalDateTime fechaHoraEntrega;

    @Column(name = "direccionentrega", nullable = false)
    @Schema(description = "Dirección donde se entrega", example = "Calle 10 # 20-30, El Poblado")
    private String direccionEntrega;

    @Column(name = "ocasion")
    @Schema(description = "Ocasión del pedido (opcional)", example = "Navidad")
    private String ocasion;

    @Column(name = "estado", nullable = false)
    @Schema(description = "REGISTRADO, EN_ELABORACION, LISTO, ENTREGADO o CANCELADO (cambia solo por /pedidos/estado)",
            example = "REGISTRADO", accessMode = Schema.AccessMode.READ_ONLY)
    private String estado;

    @Column(name = "valortotal", nullable = false)
    @Schema(description = "Suma de los arreglos (la calcula el sistema)", example = "145000",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Double valorTotal;

    @Column(name = "fechaentregareal")
    @Schema(description = "Fecha y hora real de entrega (se registra al pasar a ENTREGADO)",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaEntregaReal;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @Schema(description = "Arreglos del pedido (mínimo uno)")
    private List<Arreglo> arreglos = new ArrayList<>();

    // OneToOne (lado inverso): la tarjeta es opcional; la llave foránea está en Tarjeta.pedido.
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Tarjeta con dedicatoria (opcional)")
    private Tarjeta tarjeta;

    @PrePersist
    protected void antesDeGuardar() {
        fechaRegistro = LocalDateTime.now();
    }

    public void agregarArreglo(Arreglo arreglo) {
        arreglo.setPedido(this);
        arreglos.add(arreglo);
    }

    public void asignarTarjeta(Tarjeta nueva) {
        nueva.setPedido(this);
        this.tarjeta = nueva;
    }
}