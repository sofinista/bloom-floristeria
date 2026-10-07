package com.floristeria.bloom.controladores;

import com.floristeria.bloom.identidades.Cliente;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Clientes", description = "Gestión de clientes")
@RequestMapping("/clientes")
public interface ClienteApi {

    @Operation(summary = "Lista los clientes")
    @GetMapping("/listar")
    ResponseEntity<List<Cliente>> listar() throws SQLException;

    @Operation(summary = "Consulta un cliente activo por id")
    @GetMapping("/consultar/{id}")
    ResponseEntity<Cliente> consultar(@PathVariable Integer id) throws SQLException;

    @Operation(summary = "Crea un cliente", description = "Valida campos obligatorios y documento único")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado"),
            @ApiResponse(responseCode = "400", description = "Regla de negocio incumplida")
    })
    @PostMapping("/nuevo")
    ResponseEntity<Cliente> insertar(@RequestBody Cliente cliente) throws SQLException;

    @Operation(summary = "Actualiza un cliente")
    @PutMapping("/actualizar")
    ResponseEntity<Cliente> actualizar(@RequestBody Cliente cliente) throws SQLException;

    @Operation(summary = "Elimina (desactiva) un cliente")
    @DeleteMapping("/eliminar/{id}")
    ResponseEntity<Map<String, String>> eliminar(@PathVariable Integer id) throws SQLException;
}