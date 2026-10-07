package com.floristeria.bloom.controladores;

import com.floristeria.bloom.identidades.Pedido;
import com.floristeria.bloom.services.IPedidoService;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PedidoController implements PedidoApi {

    private final IPedidoService service;

    @Override
    public ResponseEntity<Pedido> insertar(Pedido pedido) throws SQLException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.insertar(pedido));
    }

    @Override
    public ResponseEntity<List<Pedido>> listar(String estado, Integer idCliente)
            throws SQLException {
        return ResponseEntity.ok(service.listar(estado, idCliente));
    }

    @Override
    public ResponseEntity<Pedido> consultar(Integer id) throws SQLException {
        return ResponseEntity.ok(service.consultar(id));
    }

    @Override
    public ResponseEntity<Pedido> actualizar(Pedido pedido) throws SQLException {
        return ResponseEntity.ok(service.actualizarDatos(pedido));
    }

    @Override
    public ResponseEntity<Pedido> cambiarEstado(Integer id, String estado)
            throws SQLException {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }
}