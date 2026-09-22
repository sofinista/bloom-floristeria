package com.floristeria.bloom.utilidades;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Conexion {

    @Value("${bloom.db.url}")
    private String url;

    @Value("${bloom.db.usuario}")
    private String usuario;

    @Value("${bloom.db.clave}")
    private String clave;

    // Abre una conexion nueva. Quien la use debe cerrarla (try-with-resources).
    public Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }
}