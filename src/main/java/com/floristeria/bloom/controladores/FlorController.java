package com.floristeria.bloom.controladores;

import com.floristeria.bloom.identidades.Flor;
import com.floristeria.bloom.services.IFlorService;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FlorController implements FlorApi {

    private final IFlorService service;

    @Override
    public ResponseEntity<List<Flor>> listar() throws SQLException {
        return ResponseEntity.ok(service.listar());
    }

    @Override
    public ResponseEntity<Flor> consultar(Integer id) throws SQLException {
        return ResponseEntity.ok(service.consultar(id));
    }

    @Override
    public ResponseEntity<Flor> insertar(Flor flor) throws SQLException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.insertar(flor));
    }

    @Override
    public ResponseEntity<Flor> actualizar(Flor flor) throws SQLException {
        return ResponseEntity.ok(service.actualizar(flor));
    }

    @Override
    public ResponseEntity<Map<String, String>> eliminar(Integer id) throws SQLException {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Flor eliminada (desactivada) correctamente"));
    }

    @Override
    public ResponseEntity<Flor> ajustarStock(Integer id, int cantidad) throws SQLException {
        return ResponseEntity.ok(service.ajustarStock(id, cantidad));
    }
}