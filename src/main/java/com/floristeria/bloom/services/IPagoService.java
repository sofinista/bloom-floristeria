package com.floristeria.bloom.services;

import com.floristeria.bloom.identidades.Pago;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IPagoService {

    Pago registrar(Pago pago) throws SQLException;

    List<Pago> listarPorPedido(Integer idPedido) throws SQLException;

    Map<String, Double> saldo(Integer idPedido) throws SQLException;
}