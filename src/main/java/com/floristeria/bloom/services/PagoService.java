package com.floristeria.bloom.services;

import com.floristeria.bloom.excepciones.NoEncontradoException;
import com.floristeria.bloom.excepciones.ReglaNegocioException;
import com.floristeria.bloom.identidades.Pago;
import com.floristeria.bloom.identidades.Pedido;
import com.floristeria.bloom.repositorios.jpa.PagoJpaRepository;
import com.floristeria.bloom.repositorios.jpa.PedidoJpaRepository;
import com.floristeria.bloom.utilidades.Validaciones;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PagoService implements IPagoService {

    private static final List<String> METODOS = List.of("EFECTIVO", "TARJETA", "TRANSFERENCIA");

    private final PagoJpaRepository pagoRepository;
    private final PedidoJpaRepository pedidoRepository;

    @Override
    @Transactional
    public Pago registrar(Pago pago) throws SQLException {
        validar(pago);
        pago.setMetodo(pago.getMetodo().trim().toUpperCase());
        Pedido pedido = consultarPedido(pago.getIdPedido());
        if ("CANCELADO".equals(pedido.getEstado())) {
            throw new ReglaNegocioException("No se pueden registrar pagos a un pedido CANCELADO");
        }
        double saldo = pedido.getValorTotal() - pagoRepository.totalPagado(pedido.getIdPedido());
        if (pago.getMonto() > saldo) {
            throw new ReglaNegocioException(
                    "El monto supera el saldo pendiente (" + Validaciones.redondear(saldo) + ")");
        }
        pago.setIdPago(null);
        return pagoRepository.save(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pago> listarPorPedido(Integer idPedido) throws SQLException {
        consultarPedido(idPedido);
        return pagoRepository.findByIdPedidoOrderByFechaPagoAsc(idPedido);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> saldo(Integer idPedido) throws SQLException {
        Pedido pedido = consultarPedido(idPedido);
        double pagado = pagoRepository.totalPagado(idPedido);
        return Map.of("total", pedido.getValorTotal(), "pagado", pagado,
                "saldo", Validaciones.redondear(pedido.getValorTotal() - pagado));
    }

    private void validar(Pago pago) {
        if (pago == null || pago.getIdPedido() == null) {
            throw new ReglaNegocioException("El idPedido es obligatorio");
        }
        if (pago.getMonto() == null || pago.getMonto() <= 0) {
            throw new ReglaNegocioException("El monto debe ser mayor a 0");
        }
        if (pago.getMetodo() == null || !METODOS.contains(pago.getMetodo().trim().toUpperCase())) {
            throw new ReglaNegocioException("El metodo debe ser " + METODOS);
        }
    }

    private Pedido consultarPedido(Integer idPedido) {
        if (idPedido == null || idPedido <= 0) {
            throw new NoEncontradoException("No existe un pedido con id " + idPedido);
        }
        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new NoEncontradoException("No existe un pedido con id " + idPedido));
    }
}