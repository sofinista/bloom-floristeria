package com.floristeria.bloom.controladores;

import com.floristeria.bloom.identidades.Pedido;
import com.floristeria.bloom.services.PedidoService;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService service;

    @PostMapping("/nuevo")
    public ResponseEntity<Pedido> insertar(@RequestBody Pedido pedido) throws SQLException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.insertar(pedido));
    }

    // Ejemplos: /pedidos/listar  |  /pedidos/listar?estado=LISTO  |  /pedidos/listar?idCliente=1
    @GetMapping("/listar")
    public ResponseEntity<List<Pedido>> listar(@RequestParam(required = false) String estado,
                                               @RequestParam(required = false) Integer idCliente)
            throws SQLException {
        return ResponseEntity.ok(service.listar(estado, idCliente));
    }

    @GetMapping("/consultar/{id}")
    public ResponseEntity<Pedido> consultar(@PathVariable Integer id) throws SQLException {
        return ResponseEntity.ok(service.consultar(id));
    }

    @PutMapping("/actualizar")
    public ResponseEntity<Pedido> actualizar(@RequestBody Pedido pedido) throws SQLException {
        return ResponseEntity.ok(service.actualizarDatos(pedido));
    }

    // Ejemplo: PATCH /pedidos/estado?id=1&estado=EN_ELABORACION
    @PatchMapping("/estado")
    public ResponseEntity<Pedido> cambiarEstado(@RequestParam Integer id, @RequestParam String estado)
            throws SQLException {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }
}