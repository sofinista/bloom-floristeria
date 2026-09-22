package com.floristeria.bloom.controladores;

import com.floristeria.bloom.identidades.Cliente;
import com.floristeria.bloom.services.ClienteService;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService service;

    @GetMapping("/listar")
    public ResponseEntity<List<Cliente>> listar() throws SQLException {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/consultar/{id}")
    public ResponseEntity<Cliente> consultar(@PathVariable Integer id) throws SQLException {
        return ResponseEntity.ok(service.consultar(id));
    }

    @PostMapping("/nuevo")
    public ResponseEntity<Cliente> insertar(@RequestBody Cliente cliente) throws SQLException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.insertar(cliente));
    }

    @PutMapping("/actualizar")
    public ResponseEntity<Cliente> actualizar(@RequestBody Cliente cliente) throws SQLException {
        return ResponseEntity.ok(service.actualizar(cliente));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Integer id) throws SQLException {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Cliente eliminado (desactivado) correctamente"));
    }
}
