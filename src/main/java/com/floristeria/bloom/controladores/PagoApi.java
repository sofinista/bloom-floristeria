package com.floristeria.bloom.controladores;

import com.floristeria.bloom.identidades.Pago;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Pagos", description = "Abonos y saldo de los pedidos")
@RequestMapping("/pagos")
public interface PagoApi {

    @Operation(summary = "Registra un abono", description = "No puede superar el saldo ni aplicarse a un pedido cancelado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago registrado"),
            @ApiResponse(responseCode = "400", description = "Regla de negocio incumplida")
    })
    @PostMapping("/nuevo")
    ResponseEntity<Pago> registrar(@RequestBody Pago pago) throws SQLException;

    @Operation(summary = "Lista los pagos de un pedido")
    @GetMapping("/pedido/{idPedido}")
    ResponseEntity<List<Pago>> listarPorPedido(@PathVariable Integer idPedido) throws SQLException;

    @Operation(summary = "Total, pagado y saldo de un pedido")
    @GetMapping("/saldo/{idPedido}")
    ResponseEntity<Map<String, Double>> saldo(@PathVariable Integer idPedido) throws SQLException;
}