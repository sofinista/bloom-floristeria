package com.floristeria.bloom.controladores;

import com.floristeria.bloom.excepciones.ErrorRespuesta;
import com.floristeria.bloom.identidades.Cliente;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Clientes", description = "Gestión de clientes (borrado lógico)")
@RequestMapping("/clientes")
public interface ClienteApi {

    @Operation(summary = "Lista los clientes activos",
            description = "Devuelve los clientes activos ordenados por nombre")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de clientes (puede estar vacía)"),
            @ApiResponse(responseCode = "500", description = "Error al acceder a la base de datos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @GetMapping("/listar")
    ResponseEntity<List<Cliente>> listar() throws SQLException;

    @Operation(summary = "Consulta un cliente activo por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un cliente activo con ese id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error al acceder a la base de datos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @GetMapping("/consultar/{id}")
    ResponseEntity<Cliente> consultar(
            @Parameter(description = "Id del cliente", example = "1", required = true)
            @PathVariable("id") Integer id) throws SQLException;

    @Operation(summary = "Crea un cliente",
            description = "Nombre, teléfono, tipoDocumento y numeroDocumento son obligatorios; el documento no puede repetirse")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado"),
            @ApiResponse(responseCode = "400", description = "Regla de negocio incumplida o JSON inválido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error al acceder a la base de datos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @PostMapping("/nuevo")
    ResponseEntity<Cliente> insertar(@RequestBody Cliente cliente) throws SQLException;

    @Operation(summary = "Actualiza un cliente activo",
            description = "El idCliente es obligatorio; el documento no puede pertenecer a otro cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado"),
            @ApiResponse(responseCode = "400", description = "Regla de negocio incumplida o JSON inválido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "No existe un cliente activo con ese id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error al acceder a la base de datos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @PutMapping("/actualizar")
    ResponseEntity<Cliente> actualizar(@RequestBody Cliente cliente) throws SQLException;

    @Operation(summary = "Elimina (desactiva) un cliente",
            description = "Borrado lógico: la fila se conserva con activo = false")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente desactivado"),
            @ApiResponse(responseCode = "404", description = "No existe un cliente activo con ese id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error al acceder a la base de datos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @DeleteMapping("/eliminar/{id}")
    ResponseEntity<Map<String, String>> eliminar(
            @Parameter(description = "Id del cliente", example = "1", required = true)
            @PathVariable("id") Integer id) throws SQLException;
}