package com.floristeria.bloom.services;

import com.floristeria.bloom.identidades.Cliente;
import java.sql.SQLException;
import java.util.List;

public interface IClienteService {
    List<Cliente> listar() throws SQLException;
    Cliente consultar(Integer id) throws SQLException;
    Cliente insertar(Cliente cliente) throws SQLException;
    Cliente actualizar(Cliente cliente) throws SQLException;
    void eliminar(Integer id) throws SQLException;
}