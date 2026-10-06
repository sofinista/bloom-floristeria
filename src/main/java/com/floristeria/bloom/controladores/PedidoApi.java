package com.floristeria.bloom.controladores;

import com.floristeria.bloom.identidades.Pedido;
import io.swagger.v3.oas.annotations.Operation;
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
            description = "Calcula el total y guarda todo en una transacción")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido creado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Pedido.class))),
            @ApiResponse(responseCode = "400", description = "Regla de negocio incumplida")
    })
    @PostMapping("/nuevo")
    ResponseEntity<Pedido> insertar(@RequestBody Pedido pedido) throws SQLException;

    @Operation(summary = "Lista pedidos",
            description = "Ejemplos: /pedidos/listar, /pedidos/listar?estado=LISTO, /pedidos/listar?idCliente=1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pedidos")
    })
    @GetMapping("/listar")
    ResponseEntity<List<Pedido>> listar(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "idCliente", required = false) Integer idCliente)
            throws SQLException;

    @Operation(summary = "Consulta un pedido por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "El pedido no existe")
    })
    @GetMapping("/consultar/{id}")
    ResponseEntity<Pedido> consultar(@PathVariable("id") Integer id) throws SQLException;

    @Operation(summary = "Actualiza los datos de un pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido actualizado"),
            @ApiResponse(responseCode = "400", description = "Regla de negocio incumplida"),
            @ApiResponse(responseCode = "404", description = "El pedido no existe")
    })
    @PutMapping("/actualizar")
    ResponseEntity<Pedido> actualizar(@RequestBody Pedido pedido) throws SQLException;

    @Operation(summary = "Cambia el estado de un pedido",
            description = "Solo permite las transiciones válidas del flujo. Ejemplo: PATCH /pedidos/estado?id=1&estado=EN_ELABORACION")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Transición no permitida"),
            @ApiResponse(responseCode = "404", description = "El pedido no existe")
    })
    @PatchMapping("/estado")
    ResponseEntity<Pedido> cambiarEstado(@RequestParam("id") Integer id,
                                         @RequestParam("estado") String estado)
            throws SQLException;
}