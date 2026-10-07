package com.floristeria.bloom.repositorios;

import com.floristeria.bloom.identidades.Flor;
import java.sql.SQLException;
import java.util.List;

public interface IFlorRepository {
    List<Flor> listar() throws SQLException;
    Flor consultarPorId(int id) throws SQLException;
    Flor insertar(Flor flor) throws SQLException;
    boolean actualizar(Flor flor) throws SQLException;
    boolean desactivar(int id) throws SQLException;
    boolean ajustarStock(int id, int cantidad) throws SQLException;
}