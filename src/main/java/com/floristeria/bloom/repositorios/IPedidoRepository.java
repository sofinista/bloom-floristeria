package com.floristeria.bloom.repositorios;

import com.floristeria.bloom.identidades.Pedido;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface IPedidoRepository {
    Pedido insertarConArreglos(Pedido pedido) throws SQLException;
    List<Pedido> listar(String estado, Integer idCliente) throws SQLException;
    Pedido consultarPorId(int idPedido) throws SQLException;
    boolean actualizarDatos(Pedido pedido) throws SQLException;
    boolean actualizarEstado(int idPedido, String estado, LocalDateTime fechaEntregaReal) throws SQLException;
}
