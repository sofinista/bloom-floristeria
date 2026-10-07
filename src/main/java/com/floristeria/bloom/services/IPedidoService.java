package com.floristeria.bloom.services;

import com.floristeria.bloom.identidades.Pedido;
import java.sql.SQLException;
import java.util.List;

public interface IPedidoService {
    Pedido insertar(Pedido pedido) throws SQLException;
    List<Pedido> listar(String estado, Integer idCliente) throws SQLException;
    Pedido consultar(Integer id) throws SQLException;
    Pedido actualizarDatos(Pedido datos) throws SQLException;
    Pedido cambiarEstado(Integer id, String nuevoEstado) throws SQLException;
}