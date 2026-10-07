package com.floristeria.bloom.repositorios;

import com.floristeria.bloom.identidades.Pago;
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
public class PagoRepository implements IPagoRepository {

    private final Conexion conexion;

    @Override
    public Pago insertar(Pago pago) throws SQLException {
        String sql = "INSERT INTO pago (idpedido, monto, metodo) VALUES (?, ?, ?)";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pago.getIdPedido());
            ps.setDouble(2, pago.getMonto());
            ps.setString(3, pago.getMetodo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    pago.setIdPago(keys.getInt(1));
                }
            }
        }
        return pago;
    }

    @Override
    public List<Pago> listarPorPedido(int idPedido) throws SQLException {
        String sql = "SELECT idpago, idpedido, monto, metodo, fechapago FROM pago "
                + "WHERE idpedido = ? ORDER BY fechapago";
        List<Pago> pagos = new ArrayList<>();
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pagos.add(Pago.builder()
                            .idPago(rs.getInt("idpago"))
                            .idPedido(rs.getInt("idpedido"))
                            .monto(rs.getDouble("monto"))
                            .metodo(rs.getString("metodo"))
                            .fechaPago(rs.getTimestamp("fechapago").toLocalDateTime())
                            .build());
                }
            }
        }
        return pagos;
    }

    @Override
    public double totalPagado(int idPedido) throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto), 0) FROM pago WHERE idpedido = ?";
        try (Connection con = conexion.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }
}