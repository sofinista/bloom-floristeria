package com.floristeria.bloom.controladores;

import com.floristeria.bloom.excepciones.ErrorRespuesta;
import com.floristeria.bloom.identidades.Flor;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Flores", description = "Inventario de flores (borrado lógico)")
@RequestMapping("/flores")
public interface FlorApi {

    @Operation(summary = "Lista las flores activas",
            description = "Devuelve las flores activas ordenadas por nombre")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de flores (puede estar vacía)"),
            @ApiResponse(responseCode = "500", description = "Error al acceder a la base de datos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @GetMapping("/listar")
    ResponseEntity<List<Flor>> listar() throws SQLException;

    @Operation(summary = "Consulta una flor activa por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flor encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una flor activa con ese id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error al acceder a la base de datos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @GetMapping("/consultar/{id}")
    ResponseEntity<Flor> consultar(
            @Parameter(description = "Id de la flor", example = "1", required = true)
            @PathVariable("id") Integer id) throws SQLException;

    @Operation(summary = "Crea una flor con su stock inicial",
            description = "Nombre y color obligatorios; precioUnitario mayor a 0; stock 0 o mayor")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Flor creada"),
            @ApiResponse(responseCode = "400", description = "Regla de negocio incumplida o JSON inválido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error al acceder a la base de datos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @PostMapping("/nueva")
    ResponseEntity<Flor> insertar(@RequestBody Flor flor) throws SQLException;

    @Operation(summary = "Actualiza nombre, color y precio",
            description = "El idFlor es obligatorio. El stock no se cambia aquí: use /flores/stock")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flor actualizada"),
            @ApiResponse(responseCode = "400", description = "Regla de negocio incumplida o JSON inválido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "No existe una flor activa con ese id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error al acceder a la base de datos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @PutMapping("/actualizar")
    ResponseEntity<Flor> actualizar(@RequestBody Flor flor) throws SQLException;

    @Operation(summary = "Borrado lógico de una flor",
            description = "La fila se conserva con activo = false")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flor desactivada"),
            @ApiResponse(responseCode = "404", description = "No existe una flor activa con ese id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error al acceder a la base de datos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @DeleteMapping("/eliminar/{id}")
    ResponseEntity<Map<String, String>> eliminar(
            @Parameter(description = "Id de la flor", example = "1", required = true)
            @PathVariable("id") Integer id) throws SQLException;

    @Operation(summary = "Suma o resta stock",
            description = "Cantidad positiva suma, negativa resta; no puede ser 0 ni dejar el stock negativo. "
                    + "Ejemplo: PATCH /flores/stock?id=1&cantidad=-5")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock ajustado; devuelve la flor actualizada"),
            @ApiResponse(responseCode = "400", description = "Cantidad 0, stock insuficiente o parámetro inválido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "404", description = "No existe una flor activa con ese id",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class))),
            @ApiResponse(responseCode = "500", description = "Error al acceder a la base de datos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorRespuesta.class)))
    })
    @PatchMapping("/stock")
    ResponseEntity<Flor> ajustarStock(
            @Parameter(description = "Id de la flor", example = "1", required = true)
            @RequestParam("id") Integer id,
            @Parameter(description = "Unidades a sumar (positivo) o restar (negativo)", example = "10", required = true)
            @RequestParam("cantidad") int cantidad) throws SQLException;
}