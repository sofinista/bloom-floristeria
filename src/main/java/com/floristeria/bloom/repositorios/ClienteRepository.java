package com.floristeria.bloom.repositorios;

import com.floristeria.bloom.identidades.Cliente;
import com.floristeria.bloom.utilidades.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ClienteRepository {

    private static final String COLUMNAS =
            "idcliente, nombre, telefono, direccion, correoelectronico, tipodocumento, numerodocumento, activo";

    @Autowired
    private Conexion conexion;

    // Lista solo los clientes activos
    public List<Cliente> listar() throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM cliente WHERE activo = 1 ORDER BY nombre";
        List<Cliente> clientes = new ArrayList<>();
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(mapear(rs));
            }
        }
        return clientes;
    }

    // Devuelve el cliente (activo o no) o null si no existe
    public Cliente consultarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM cliente WHERE idcliente = ?";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public Cliente insertar(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO cliente (nombre, telefono, direccion, correoelectronico, "
                + "tipodocumento, numerodocumento) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTelefono());
            ps.setString(3, cliente.getDireccion());
            ps.setString(4, cliente.getCorreoElectronico());
            ps.setString(5, cliente.getTipoDocumento());
            ps.setString(6, cliente.getNumeroDocumento());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    cliente.setIdCliente(keys.getInt(1));
                }
            }
        }
        cliente.setActivo(true);
        return cliente;
    }

    public boolean actualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE cliente SET nombre = ?, telefono = ?, direccion = ?, correoelectronico = ?, "
                + "tipodocumento = ?, numerodocumento = ? WHERE idcliente = ? AND activo = 1";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getTelefono());
            ps.setString(3, cliente.getDireccion());
            ps.setString(4, cliente.getCorreoElectronico());
            ps.setString(5, cliente.getTipoDocumento());
            ps.setString(6, cliente.getNumeroDocumento());
            ps.setInt(7, cliente.getIdCliente());
            return ps.executeUpdate() > 0;
        }
    }

    // Borrado logico: no se borra la fila, solo se marca como inactivo
    public boolean desactivar(int id) throws SQLException {
        String sql = "UPDATE cliente SET activo = 0 WHERE idcliente = ? AND activo = 1";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Revisa si el documento ya lo tiene OTRO cliente (excluirId = 0 al crear)
    public boolean existeDocumento(String numeroDocumento, int excluirId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cliente WHERE numerodocumento = ? AND idcliente <> ?";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numeroDocumento);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return Cliente.builder()
                .idCliente(rs.getInt("idcliente"))
                .nombre(rs.getString("nombre"))
                .telefono(rs.getString("telefono"))
                .direccion(rs.getString("direccion"))
                .correoElectronico(rs.getString("correoelectronico"))
                .tipoDocumento(rs.getString("tipodocumento"))
                .numeroDocumento(rs.getString("numerodocumento"))
                .activo(rs.getBoolean("activo"))
                .build();
    }
}