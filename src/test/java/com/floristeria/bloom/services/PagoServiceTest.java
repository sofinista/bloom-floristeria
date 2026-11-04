package com.floristeria.bloom.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.floristeria.bloom.excepciones.NoEncontradoException;
import com.floristeria.bloom.excepciones.ReglaNegocioException;
import com.floristeria.bloom.identidades.Pago;
import com.floristeria.bloom.identidades.Pedido;
import com.floristeria.bloom.repositorios.jpa.PagoJpaRepository;
import com.floristeria.bloom.repositorios.jpa.PedidoJpaRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    PagoJpaRepository pagoRepository;

    @Mock
    PedidoJpaRepository pedidoRepository;

    @InjectMocks
    PagoService service;

    // ---------- datos de apoyo ----------

    private Pedido pedido(String estado, double total) {
        return Pedido.builder().idPedido(1).estado(estado).valorTotal(total).build();
    }

    private Pago pagoValido() {
        return Pago.builder().idPedido(1).monto(50000.0).metodo("efectivo").build();
    }

    // ---------- registrar ----------

    @Test
    void registrar_conDatosValidos_guardaElPagoConMetodoEnMayusculas() throws SQLException {
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido("REGISTRADO", 145000.0)));
        when(pagoRepository.totalPagado(1)).thenReturn(0.0);
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        Pago resultado = service.registrar(pagoValido());

        assertEquals("EFECTIVO", resultado.getMetodo());
        assertEquals(50000.0, resultado.getMonto().doubleValue(), 0.001);
        assertNull(resultado.getIdPago());
        verify(pagoRepository).save(any(Pago.class));
    }

    @Test
    void registrar_conMontoIgualAlSaldo_guardaElPago() throws SQLException {
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido("LISTO", 145000.0)));
        when(pagoRepository.totalPagado(1)).thenReturn(95000.0);
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        Pago resultado = service.registrar(pagoValido());

        assertEquals(50000.0, resultado.getMonto().doubleValue(), 0.001);
    }

    @Test
    void registrar_conMontoQueSuperaElSaldo_lanzaReglaNegocioYNoGuarda() throws SQLException {
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido("REGISTRADO", 145000.0)));
        when(pagoRepository.totalPagado(1)).thenReturn(100000.0);

        assertThrows(ReglaNegocioException.class, () -> service.registrar(pagoValido()));

        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void registrar_aPedidoCancelado_lanzaReglaNegocioYNoGuarda() throws SQLException {
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido("CANCELADO", 145000.0)));

        assertThrows(ReglaNegocioException.class, () -> service.registrar(pagoValido()));

        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void registrar_aPedidoInexistente_lanzaNoEncontrado() {
        when(pedidoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NoEncontradoException.class, () -> service.registrar(pagoValido()));
    }

    @Test
    void registrar_pagoNulo_lanzaReglaNegocio() {
        assertThrows(ReglaNegocioException.class, () -> service.registrar(null));
    }

    @Test
    void registrar_sinIdPedido_lanzaReglaNegocio() {
        Pago pago = pagoValido();
        pago.setIdPedido(null);

        assertThrows(ReglaNegocioException.class, () -> service.registrar(pago));
    }

    @Test
    void registrar_sinMonto_lanzaReglaNegocio() {
        Pago pago = pagoValido();
        pago.setMonto(null);

        assertThrows(ReglaNegocioException.class, () -> service.registrar(pago));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -5000})
    void registrar_conMontoNoPositivo_lanzaReglaNegocio(double monto) {
        Pago pago = pagoValido();
        pago.setMonto(monto);

        assertThrows(ReglaNegocioException.class, () -> service.registrar(pago));
    }

    @Test
    void registrar_sinMetodo_lanzaReglaNegocio() {
        Pago pago = pagoValido();
        pago.setMetodo(null);

        assertThrows(ReglaNegocioException.class, () -> service.registrar(pago));
    }

    @Test
    void registrar_conMetodoInvalido_lanzaReglaNegocio() {
        Pago pago = pagoValido();
        pago.setMetodo("CHEQUE");

        assertThrows(ReglaNegocioException.class, () -> service.registrar(pago));
    }

    // ---------- listarPorPedido ----------

    @Test
    void listarPorPedido_conPedidoExistente_devuelveSusPagos() throws SQLException {
        List<Pago> esperados = List.of(pagoValido());
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido("REGISTRADO", 145000.0)));
        when(pagoRepository.findByIdPedidoOrderByFechaPagoAsc(1)).thenReturn(esperados);

        List<Pago> resultado = service.listarPorPedido(1);

        assertEquals(esperados, resultado);
    }

    @Test
    void listarPorPedido_conPedidoInexistente_lanzaNoEncontradoYNoConsultaPagos() {
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoEncontradoException.class, () -> service.listarPorPedido(99));

        verify(pagoRepository, never()).findByIdPedidoOrderByFechaPagoAsc(any());
    }

    @Test
    void listarPorPedido_conIdNulo_lanzaNoEncontrado() {
        assertThrows(NoEncontradoException.class, () -> service.listarPorPedido(null));
    }

    // ---------- saldo ----------

    @Test
    void saldo_conAbonos_calculaTotalPagadoYSaldo() throws SQLException {
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido("REGISTRADO", 145000.0)));
        when(pagoRepository.totalPagado(1)).thenReturn(50000.0);

        Map<String, Double> resultado = service.saldo(1);

        assertEquals(145000.0, resultado.get("total").doubleValue(), 0.001);
        assertEquals(50000.0, resultado.get("pagado").doubleValue(), 0.001);
        assertEquals(95000.0, resultado.get("saldo").doubleValue(), 0.001);
    }

    @Test
    void saldo_sinAbonos_elSaldoEsElTotal() throws SQLException {
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido("REGISTRADO", 145000.0)));
        when(pagoRepository.totalPagado(1)).thenReturn(0.0);

        Map<String, Double> resultado = service.saldo(1);

        assertEquals(145000.0, resultado.get("saldo").doubleValue(), 0.001);
    }

    @Test
    void saldo_conPedidoInexistente_lanzaNoEncontrado() {
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoEncontradoException.class, () -> service.saldo(99));
    }
}