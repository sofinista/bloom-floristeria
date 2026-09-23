package com.floristeria.bloom.repositorios;

import com.floristeria.bloom.identidades.Arreglo;
import com.floristeria.bloom.identidades.Pedido;
import com.floristeria.bloom.utilidades.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PedidoRepository {

    private static final String COLUMNAS = "idpedido, idcliente, fecharegistro, fechahoraentrega, "
            + "direccionentrega, ocasion, estado, valortotal, fechaentregareal";

    @Autowired
    private Conexion conexion;

    // Guarda el pedido y todos sus arreglos en UNA transaccion:
    // o se guarda todo, o no se guarda nada (rollback).
    public Pedido insertarConArreglos(Pedido pedido) throws SQLException {
        String sqlPedido = "INSERT INTO pedido (idcliente, fechahoraentrega, direccionentrega, ocasion, "
                + "estado, valortotal) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlArreglo = "INSERT INTO arreglo (idpedido, tipoarreglo, tamano, colores, descripcion, "
                + "valorunitario) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = conexion.obtenerConexion()) {
            con.setAutoCommit(false); // inicia la transaccion
            try {
                int idPedido;
                try (PreparedStatement ps = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, pedido.getIdCliente());
                    ps.setTimestamp(2, Timestamp.valueOf(pedido.getFechaHoraEntrega()));
                    ps.setString(3, pedido.getDireccionEntrega());
                    ps.setString(4, pedido.getOcasion());
                    ps.setString(5, pedido.getEstado());
                    ps.setDouble(6, pedido.getValorTotal());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        idPedido = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sqlArreglo)) {
                    for (Arreglo arreglo : pedido.getArreglos()) {
                        ps.setInt(1, idPedido);
                        ps.setString(2, arreglo.getTipoArreglo());
                        ps.setString(3, arreglo.getTamano());
                        ps.setString(4, arreglo.getColores());
                        ps.setString(5, arreglo.getDescripcion());
                        ps.setDouble(6, arreglo.getValorUnitario());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                con.commit(); // confirma todo
                pedido.setIdPedido(idPedido);
                return pedido;
            } catch (SQLException | RuntimeException e) {
                con.rollback(); // deshace todo si algo fallo
                throw e;
            }
        }
    }

    // Lista pedidos. Los filtros son opcionales (pueden venir null).
    public List<Pedido> listar(String estado, Integer idCliente) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNAS + " FROM pedido WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (estado != null) {
            sql.append(" AND estado = ?");
            parametros.add(estado);
        }
        if (idCliente != null) {
            sql.append(" AND idcliente = ?");
            parametros.add(idCliente);
        }
        sql.append(" ORDER BY fechahoraentrega");

        List<Pedido> pedidos = new ArrayList<>();
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapear(rs));
                }
            }
        }
        return pedidos;
    }

    // Devuelve el pedido con sus arreglos, o null si no existe
    public Pedido consultarPorId(int idPedido) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM pedido WHERE idpedido = ?";
        try (Connection con = conexion.obtenerConexion()) {
            Pedido pedido = null;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        pedido = mapear(rs);
                    }
                }
            }
            if (pedido != null) {
                pedido.setArreglos(consultarArreglos(con, idPedido));
            }
            return pedido;
        }
    }

    // Solo actualiza si el pedido sigue en estado REGISTRADO
    public boolean actualizarDatos(Pedido pedido) throws SQLException {
        String sql = "UPDATE pedido SET fechahoraentrega = ?, direccionentrega = ?, ocasion = ? "
                + "WHERE idpedido = ? AND estado = 'REGISTRADO'";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(pedido.getFechaHoraEntrega()));
            ps.setString(2, pedido.getDireccionEntrega());
            ps.setString(3, pedido.getOcasion());
            ps.setInt(4, pedido.getIdPedido());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarEstado(int idPedido, String estado, LocalDateTime fechaEntregaReal)
            throws SQLException {
        String sql = "UPDATE pedido SET estado = ?, fechaentregareal = ? WHERE idpedido = ?";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            if (fechaEntregaReal == null) {
                ps.setNull(2, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(2, Timestamp.valueOf(fechaEntregaReal));
            }
            ps.setInt(3, idPedido);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Arreglo> consultarArreglos(Connection con, int idPedido) throws SQLException {
        String sql = "SELECT idarreglo, idpedido, tipoarreglo, tamano, colores, descripcion, valorunitario "
                + "FROM arreglo WHERE idpedido = ? ORDER BY idarreglo";
        List<Arreglo> arreglos = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    arreglos.add(Arreglo.builder()
                            .idArreglo(rs.getInt("idarreglo"))
                            .idPedido(rs.getInt("idpedido"))
                            .tipoArreglo(rs.getString("tipoarreglo"))
                            .tamano(rs.getString("tamano"))
                            .colores(rs.getString("colores"))
                            .descripcion(rs.getString("descripcion"))
                            .valorUnitario(rs.getDouble("valorunitario"))
                            .build());
                }
            }
        }
        return arreglos;
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Timestamp entregaReal = rs.getTimestamp("fechaentregareal");
        return Pedido.builder()
                .idPedido(rs.getInt("idpedido"))
                .idCliente(rs.getInt("idcliente"))
                .fechaRegistro(rs.getTimestamp("fecharegistro").toLocalDateTime())
                .fechaHoraEntrega(rs.getTimestamp("fechahoraentrega").toLocalDateTime())
                .direccionEntrega(rs.getString("direccionentrega"))
                .ocasion(rs.getString("ocasion"))
                .estado(rs.getString("estado"))
                .valorTotal(rs.getDouble("valortotal"))
                .fechaEntregaReal(entregaReal == null ? null : entregaReal.toLocalDateTime())
                .build();
    }
}