package com.floristeria.bloom.services;

import com.floristeria.bloom.identidades.Flor;
import java.sql.SQLException;
import java.util.List;

public interface IFlorService {

    List<Flor> listar() throws SQLException;

    Flor consultar(Integer id) throws SQLException;

    Flor insertar(Flor flor) throws SQLException;

    Flor actualizar(Flor flor) throws SQLException;

    void eliminar(Integer id) throws SQLException;

    Flor ajustarStock(Integer id, int cantidad) throws SQLException;
}