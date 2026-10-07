package com.floristeria.bloom.controladores;

import com.floristeria.bloom.identidades.Pago;
import com.floristeria.bloom.services.IPagoService;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PagoController implements PagoApi {

    private final IPagoService service;

    @Override
    public ResponseEntity<Pago> registrar(Pago pago) throws SQLException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(pago));
    }

    @Override
    public ResponseEntity<List<Pago>> listarPorPedido(Integer idPedido) throws SQLException {
        return ResponseEntity.ok(service.listarPorPedido(idPedido));
    }

    @Override
    public ResponseEntity<Map<String, Double>> saldo(Integer idPedido) throws SQLException {
        return ResponseEntity.ok(service.saldo(idPedido));
    }
}