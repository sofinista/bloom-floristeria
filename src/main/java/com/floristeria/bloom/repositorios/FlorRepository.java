package com.floristeria.bloom.repositorios;

import com.floristeria.bloom.identidades.Flor;
import com.floristeria.bloom.utilidades.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FlorRepository implements IFlorRepository {

    private static final String COLUMNAS = "idflor, nombre, color, stock, preciounitario, activo";

    private final Conexion conexion;

    @Override
    public List<Flor> listar() throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM flor WHERE activo = 1 ORDER BY nombre";
        List<Flor> flores = new ArrayList<>();
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                flores.add(mapear(rs));
            }
        }
        return flores;
    }

    @Override
    public Flor consultarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM flor WHERE idflor = ?";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Flor insertar(Flor flor) throws SQLException {
        String sql = "INSERT INTO flor (nombre, color, stock, preciounitario) VALUES (?, ?, ?, ?)";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, flor.getNombre());
            ps.setString(2, flor.getColor());
            ps.setInt(3, flor.getStock());
            ps.setDouble(4, flor.getPrecioUnitario());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    flor.setIdFlor(keys.getInt(1));
                }
            }
        }
        flor.setActivo(true);
        return flor;
    }

    @Override
    public boolean actualizar(Flor flor) throws SQLException {
        String sql = "UPDATE flor SET nombre = ?, color = ?, preciounitario = ? WHERE idflor = ? AND activo = 1";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, flor.getNombre());
            ps.setString(2, flor.getColor());
            ps.setDouble(3, flor.getPrecioUnitario());
            ps.setInt(4, flor.getIdFlor());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean desactivar(int id) throws SQLException {
        String sql = "UPDATE flor SET activo = 0 WHERE idflor = ? AND activo = 1";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Una sola sentencia: si el stock quedaria negativo, no actualiza nada
    @Override
    public boolean ajustarStock(int id, int cantidad) throws SQLException {
        String sql = "UPDATE flor SET stock = stock + ? WHERE idflor = ? AND activo = 1 AND stock + ? >= 0";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, id);
            ps.setInt(3, cantidad);
            return ps.executeUpdate() > 0;
        }
    }

    private Flor mapear(ResultSet rs) throws SQLException {
        return Flor.builder()
                .idFlor(rs.getInt("idflor"))
                .nombre(rs.getString("nombre"))
                .color(rs.getString("color"))
                .stock(rs.getInt("stock"))
                .precioUnitario(rs.getDouble("preciounitario"))
                .activo(rs.getBoolean("activo"))
                .build();
    }
}