package com.floristeria.bloom.services;

import com.floristeria.bloom.excepciones.NoEncontradoException;
import com.floristeria.bloom.excepciones.ReglaNegocioException;
import com.floristeria.bloom.identidades.Pago;
import com.floristeria.bloom.identidades.Pedido;
import com.floristeria.bloom.repositorios.IPagoRepository;
import com.floristeria.bloom.repositorios.IPedidoRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PagoService implements IPagoService {

    private static final List<String> METODOS = List.of("EFECTIVO", "TARJETA", "TRANSFERENCIA");

    private final IPagoRepository pagoRepository;
    private final IPedidoRepository pedidoRepository;

    @Override
    public Pago registrar(Pago pago) throws SQLException {
        if (pago == null || pago.getIdPedido() == null) {
            throw new ReglaNegocioException("El idPedido es obligatorio");
        }
        if (pago.getMonto() == null || pago.getMonto() <= 0) {
            throw new ReglaNegocioException("El monto debe ser mayor a 0");
        }
        if (pago.getMetodo() == null || !METODOS.contains(pago.getMetodo().trim().toUpperCase())) {
            throw new ReglaNegocioException("El metodo debe ser " + METODOS);
        }
        pago.setMetodo(pago.getMetodo().trim().toUpperCase());
        Pedido pedido = consultarPedido(pago.getIdPedido());
        if ("CANCELADO".equals(pedido.getEstado())) {
            throw new ReglaNegocioException("No se pueden registrar pagos a un pedido CANCELADO");
        }
        double saldo = pedido.getValorTotal() - pagoRepository.totalPagado(pedido.getIdPedido());
        if (pago.getMonto() > saldo) {
            throw new ReglaNegocioException("El monto supera el saldo pendiente (" + saldo + ")");
        }
        return pagoRepository.insertar(pago);
    }

    @Override
    public List<Pago> listarPorPedido(Integer idPedido) throws SQLException {
        consultarPedido(idPedido);
        return pagoRepository.listarPorPedido(idPedido);
    }

    @Override
    public Map<String, Double> saldo(Integer idPedido) throws SQLException {
        Pedido pedido = consultarPedido(idPedido);
        double pagado = pagoRepository.totalPagado(idPedido);
        return Map.of("total", pedido.getValorTotal(), "pagado", pagado,
                "saldo", Math.round((pedido.getValorTotal() - pagado) * 100.0) / 100.0);
    }

    private Pedido consultarPedido(Integer idPedido) throws SQLException {
        Pedido pedido = (idPedido == null || idPedido <= 0) ? null : pedidoRepository.consultarPorId(idPedido);
        if (pedido == null) {
            throw new NoEncontradoException("No existe un pedido con id " + idPedido);
        }
        return pedido;
    }
}