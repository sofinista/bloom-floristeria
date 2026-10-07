package com.floristeria.bloom.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.floristeria.bloom.excepciones.ReglaNegocioException;
import com.floristeria.bloom.identidades.Pago;
import com.floristeria.bloom.identidades.Pedido;
import com.floristeria.bloom.repositorios.IPagoRepository;
import com.floristeria.bloom.repositorios.IPedidoRepository;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private IPagoRepository pagoRepository;

    @Mock
    private IPedidoRepository pedidoRepository;

    @InjectMocks
    private PagoService service;

    @Test
    void registrar_conMontoMayorAlSaldo_lanzaReglaNegocio() throws SQLException {
        Pedido pedido = Pedido.builder().idPedido(1).estado("REGISTRADO").valorTotal(100000.0).build();
        when(pedidoRepository.consultarPorId(1)).thenReturn(pedido);
        when(pagoRepository.totalPagado(1)).thenReturn(80000.0);
        Pago pago = Pago.builder().idPedido(1).monto(30000.0).metodo("EFECTIVO").build();

        assertThrows(ReglaNegocioException.class, () -> service.registrar(pago));
    }

    @Test
    void registrar_conMetodoInvalido_lanzaReglaNegocio() {
        Pago pago = Pago.builder().idPedido(1).monto(1000.0).metodo("BITCOIN").build();

        assertThrows(ReglaNegocioException.class, () -> service.registrar(pago));
    }
}