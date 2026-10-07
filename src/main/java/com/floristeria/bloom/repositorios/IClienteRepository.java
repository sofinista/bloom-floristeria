package com.floristeria.bloom.repositorios;

import com.floristeria.bloom.identidades.Cliente;
import java.sql.SQLException;
import java.util.List;


public interface IClienteRepository {
    List<Cliente> listar() throws SQLException;
    Cliente consultarPorId(int id) throws SQLException;
    Cliente insertar(Cliente cliente) throws SQLException;
    boolean actualizar(Cliente cliente) throws SQLException;
    boolean desactivar(int id) throws SQLException;
    boolean existeDocumento(String numeroDocumento, int excluirId) throws SQLException;
}