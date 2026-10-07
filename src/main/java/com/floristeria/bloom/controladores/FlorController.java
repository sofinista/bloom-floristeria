package com.floristeria.bloom.controladores;

import com.floristeria.bloom.identidades.Flor;
import com.floristeria.bloom.services.IFlorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Flores", description = "Inventario de flores")
@RestController
@RequestMapping("/flores")
@RequiredArgsConstructor
public class FlorController {

    private final IFlorService service;

    @Operation(summary = "Lista las flores activas")
    @GetMapping("/listar")
    public ResponseEntity<List<Flor>> listar() throws SQLException {
        return ResponseEntity.ok(service.listar());
    }

    @Operation(summary = "Consulta una flor por id")
    @ApiResponse(responseCode = "404", description = "No existe")
    @GetMapping("/consultar/{id}")
    public ResponseEntity<Flor> consultar(@PathVariable Integer id) throws SQLException {
        return ResponseEntity.ok(service.consultar(id));
    }

    @Operation(summary = "Crea una flor con su stock inicial")
    @PostMapping("/nueva")
    public ResponseEntity<Flor> insertar(@RequestBody Flor flor) throws SQLException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.insertar(flor));
    }

    @Operation(summary = "Actualiza nombre, color y precio")
    @PutMapping("/actualizar")
    public ResponseEntity<Flor> actualizar(@RequestBody Flor flor) throws SQLException {
        return ResponseEntity.ok(service.actualizar(flor));
    }

    @Operation(summary = "Borrado lógico de una flor")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Integer id) throws SQLException {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Flor eliminada (desactivada) correctamente"));
    }

    @Operation(summary = "Suma o resta stock",
            description = "cantidad positiva suma, negativa resta; no puede quedar negativo")
    @PatchMapping("/stock")
    public ResponseEntity<Flor> ajustarStock(@RequestParam Integer id, @RequestParam int cantidad)
            throws SQLException {
        return ResponseEntity.ok(service.ajustarStock(id, cantidad));
    }
}