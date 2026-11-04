package com.floristeria.bloom.controladores;

import com.floristeria.bloom.excepciones.ErrorRespuesta;
import com.floristeria.bloom.identidades.Pedido;
import com.floristeria.bloom.identidades.Tarjeta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.sql.SQLException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Pedidos", description = "Registro y seguimiento de pedidos")
@RequestMapping("/pedidos")
public interface PedidoApi {

    @Operation(summary = "Registra un pedido con sus arreglos",
            description = "Valida al cliente, calcula el total y guarda pedido, arreglos y tarjeta (opcional) en una transacción")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido creado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Pedido.class))),
            @ApiResponse(responseCode = "400", description = "Regla de negocio incumplida o JSON inválido",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @PostMapping("/nuevo")
    ResponseEntity<Pedido> insertar(@RequestBody Pedido pedido) throws SQLException;

    @Operation(summary = "Lista pedidos",
            description = "Ordenados por fecha de entrega. Ejemplos: /pedidos/listar, /pedidos/listar?estado=LISTO, /pedidos/listar?idCliente=1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pedidos (puede venir vacía)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = Pedido.class)))),
            @ApiResponse(responseCode = "400", description = "Estado no válido",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @GetMapping("/listar")
    ResponseEntity<List<Pedido>> listar(
            @Parameter(description = "Filtra por estado: REGISTRADO, EN_ELABORACION, LISTO, ENTREGADO o CANCELADO",
                    example = "LISTO")
            @RequestParam(value = "estado", required = false) String estado,
            @Parameter(description = "Filtra por id del cliente", example = "1")
            @RequestParam(value = "idCliente", required = false) Integer idCliente)
            throws SQLException;

    @Operation(summary = "Consulta un pedido por id",
            description = "Devuelve el pedido con sus arreglos y su tarjeta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Pedido.class))),
            @ApiResponse(responseCode = "404", description = "El pedido no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @GetMapping("/consultar/{id}")
    ResponseEntity<Pedido> consultar(
            @Parameter(description = "Id del pedido", example = "1")
            @PathVariable("id") Integer id) throws SQLException;

    @Operation(summary = "Actualiza los datos de un pedido",
            description = "Solo cambia fecha de entrega, dirección y ocasión, y solo si el pedido está REGISTRADO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido actualizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Pedido.class))),
            @ApiResponse(responseCode = "400", description = "Regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "El pedido no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @PutMapping("/actualizar")
    ResponseEntity<Pedido> actualizar(@RequestBody Pedido pedido) throws SQLException;

    @Operation(summary = "Cambia el estado de un pedido",
            description = "Solo permite las transiciones válidas del flujo. Ejemplo: PATCH /pedidos/estado?id=1&estado=EN_ELABORACION")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Pedido.class))),
            @ApiResponse(responseCode = "400", description = "Estado inválido o transición no permitida",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "El pedido no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @PatchMapping("/estado")
    ResponseEntity<Pedido> cambiarEstado(
            @Parameter(description = "Id del pedido", example = "1")
            @RequestParam("id") Integer id,
            @Parameter(description = "Nuevo estado", example = "EN_ELABORACION")
            @RequestParam("estado") String estado)
            throws SQLException;

    @Operation(summary = "Crea o reemplaza la tarjeta de un pedido",
            description = "Cada pedido tiene como máximo una tarjeta (relación uno a uno). No se puede cambiar si el pedido está ENTREGADO o CANCELADO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarjeta guardada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Pedido.class))),
            @ApiResponse(responseCode = "400", description = "Mensaje vacío, muy largo o pedido no editable",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "El pedido no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @PutMapping("/tarjeta/{id}")
    ResponseEntity<Pedido> guardarTarjeta(
            @Parameter(description = "Id del pedido", example = "1")
            @PathVariable("id") Integer id,
            @RequestBody Tarjeta tarjeta)
            throws SQLException;
}