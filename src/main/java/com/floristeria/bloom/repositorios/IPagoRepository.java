package com.floristeria.bloom.repositorios;

import com.floristeria.bloom.identidades.Pago;
import java.sql.SQLException;
import java.util.List;

public interface IPagoRepository {

    Pago insertar(Pago pago) throws SQLException;

    List<Pago> listarPorPedido(int idPedido) throws SQLException;

    double totalPagado(int idPedido) throws SQLException;
}