package com.floristeria.bloom.controladores;

import com.floristeria.bloom.identidades.Cliente;
import com.floristeria.bloom.services.IClienteService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ClienteController implements ClienteApi {

    private final IClienteService service;

    @Override
    public ResponseEntity<List<Cliente>> listar() throws SQLException {
        return ResponseEntity.ok(service.listar());
    }

    @Override
    public ResponseEntity<Cliente> consultar(Integer id) throws SQLException {
        return ResponseEntity.ok(service.consultar(id));
    }

    @Override
    public ResponseEntity<Cliente> insertar(Cliente cliente) throws SQLException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.insertar(cliente));
    }

    @Override
    public ResponseEntity<Cliente> actualizar(Cliente cliente) throws SQLException {
        return ResponseEntity.ok(service.actualizar(cliente));
    }

    @Override
    public ResponseEntity<Map<String, String>> eliminar(Integer id) throws SQLException {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Cliente eliminado (desactivado) correctamente"));
    }
}