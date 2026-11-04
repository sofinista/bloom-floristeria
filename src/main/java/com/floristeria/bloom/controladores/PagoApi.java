package com.floristeria.bloom.controladores;

import com.floristeria.bloom.excepciones.ErrorRespuesta;
import com.floristeria.bloom.identidades.Pago;
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
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Pagos", description = "Abonos y saldo de los pedidos")
@RequestMapping("/pagos")
public interface PagoApi {

    @Operation(summary = "Registra un abono",
            description = "No puede superar el saldo pendiente ni aplicarse a un pedido cancelado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago registrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Pago.class))),
            @ApiResponse(responseCode = "400", description = "Regla de negocio incumplida o JSON inválido",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "El pedido no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @PostMapping("/nuevo")
    ResponseEntity<Pago> registrar(@RequestBody Pago pago) throws SQLException;

    @Operation(summary = "Lista los pagos de un pedido", description = "Ordenados por fecha de pago")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pagos (puede venir vacía)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = Pago.class)))),
            @ApiResponse(responseCode = "404", description = "El pedido no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @GetMapping("/pedido/{idPedido}")
    ResponseEntity<List<Pago>> listarPorPedido(
            @Parameter(description = "Id del pedido", example = "1")
            @PathVariable("idPedido") Integer idPedido) throws SQLException;

    @Operation(summary = "Total, pagado y saldo de un pedido",
            description = "Devuelve un JSON con las claves total, pagado y saldo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo calculado"),
            @ApiResponse(responseCode = "404", description = "El pedido no existe",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @GetMapping("/saldo/{idPedido}")
    ResponseEntity<Map<String, Double>> saldo(
            @Parameter(description = "Id del pedido", example = "1")
            @PathVariable("idPedido") Integer idPedido) throws SQLException;
}