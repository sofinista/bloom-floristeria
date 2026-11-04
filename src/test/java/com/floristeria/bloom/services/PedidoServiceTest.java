package com.floristeria.bloom.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.floristeria.bloom.excepciones.NoEncontradoException;
import com.floristeria.bloom.excepciones.ReglaNegocioException;
import com.floristeria.bloom.identidades.Arreglo;
import com.floristeria.bloom.identidades.Cliente;
import com.floristeria.bloom.identidades.Pedido;
import com.floristeria.bloom.identidades.Tarjeta;
import com.floristeria.bloom.repositorios.IClienteRepository;
import com.floristeria.bloom.repositorios.jpa.PedidoJpaRepository;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    PedidoJpaRepository pedidoRepository;

    @Mock
    IClienteRepository clienteRepository;

    @InjectMocks
    PedidoService service;

    // ---------- datos de apoyo ----------

    private Pedido pedidoNuevoValido() {
        Pedido pedido = Pedido.builder()
                .idCliente(1)
                .fechaHoraEntrega(LocalDateTime.now().plusDays(2))
                .direccionEntrega("  Calle 10 # 20-30  ")
                .ocasion("Navidad")
                .build();
        pedido.agregarArreglo(Arreglo.builder().tipoArreglo("Ramo").valorUnitario(85000.0).build());
        pedido.agregarArreglo(Arreglo.builder().tipoArreglo("Caja").valorUnitario(60000.0).build());
        return pedido;
    }

    private Pedido pedidoGuardado(String estado) {
        return Pedido.builder()
                .idPedido(1)
                .idCliente(1)
                .fechaHoraEntrega(LocalDateTime.now().plusDays(2))
                .direccionEntrega("Calle 10")
                .estado(estado)
                .valorTotal(145000.0)
                .build();
    }

    private Cliente clienteConActivo(boolean activo) {
        Cliente cliente = mock(Cliente.class);
        when(cliente.getActivo()).thenReturn(activo);
        return cliente;
    }

    // ---------- insertar ----------

    @Test
    void insertar_conDatosValidos_calculaTotalYDejaEstadoRegistrado() throws SQLException {
        Cliente cliente = clienteConActivo(true);
        when(clienteRepository.consultarPorId(1)).thenReturn(cliente);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = service.insertar(pedidoNuevoValido());

        assertEquals("REGISTRADO", resultado.getEstado());
        assertEquals(145000.0, resultado.getValorTotal().doubleValue(), 0.001);
        assertEquals("Calle 10 # 20-30", resultado.getDireccionEntrega());
        assertEquals(2, resultado.getArreglos().size());
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void insertar_conTarjeta_enlazaLaTarjetaConElPedido() throws SQLException {
        Cliente cliente = clienteConActivo(true);
        when(clienteRepository.consultarPorId(1)).thenReturn(cliente);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        Pedido datos = pedidoNuevoValido();
        datos.setTarjeta(Tarjeta.builder().mensaje("  Feliz Navidad  ").remitente("Carlos").build());

        Pedido resultado = service.insertar(datos);

        assertNotNull(resultado.getTarjeta());
        assertEquals("Feliz Navidad", resultado.getTarjeta().getMensaje());
        assertSame(resultado, resultado.getTarjeta().getPedido());
    }

    @Test
    void insertar_pedidoNulo_lanzaReglaNegocio() {
        assertThrows(ReglaNegocioException.class, () -> service.insertar(null));
    }

    @Test
    void insertar_sinIdCliente_lanzaReglaNegocio() {
        Pedido pedido = pedidoNuevoValido();
        pedido.setIdCliente(null);

        assertThrows(ReglaNegocioException.class, () -> service.insertar(pedido));
    }

    @Test
    void insertar_conFechaDeEntregaPasada_lanzaReglaNegocio() {
        Pedido pedido = pedidoNuevoValido();
        pedido.setFechaHoraEntrega(LocalDateTime.now().minusDays(1));

        assertThrows(ReglaNegocioException.class, () -> service.insertar(pedido));
    }

    @Test
    void insertar_sinDireccion_lanzaReglaNegocio() {
        Pedido pedido = pedidoNuevoValido();
        pedido.setDireccionEntrega("   ");

        assertThrows(ReglaNegocioException.class, () -> service.insertar(pedido));
    }

    @Test
    void insertar_sinArreglos_lanzaReglaNegocio() {
        Pedido pedido = pedidoNuevoValido();
        pedido.getArreglos().clear();

        assertThrows(ReglaNegocioException.class, () -> service.insertar(pedido));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -10})
    void insertar_conValorDeArregloNoPositivo_lanzaReglaNegocio(double valor) {
        Pedido pedido = pedidoNuevoValido();
        pedido.getArreglos().get(0).setValorUnitario(valor);

        assertThrows(ReglaNegocioException.class, () -> service.insertar(pedido));
    }

    @Test
    void insertar_conTarjetaSinMensaje_lanzaReglaNegocio() {
        Pedido pedido = pedidoNuevoValido();
        pedido.setTarjeta(Tarjeta.builder().mensaje("  ").build());

        assertThrows(ReglaNegocioException.class, () -> service.insertar(pedido));
    }

    @Test
    void insertar_conClienteInactivo_lanzaReglaNegocioYNoGuarda() throws SQLException {
        Cliente cliente = clienteConActivo(false);
        when(clienteRepository.consultarPorId(1)).thenReturn(cliente);

        assertThrows(ReglaNegocioException.class, () -> service.insertar(pedidoNuevoValido()));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void insertar_conClienteInexistente_lanzaReglaNegocioYNoGuarda() throws SQLException {
        when(clienteRepository.consultarPorId(1)).thenReturn(null);

        assertThrows(ReglaNegocioException.class, () -> service.insertar(pedidoNuevoValido()));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    // ---------- listar ----------

    @Test
    void listar_sinFiltros_devuelveTodosLosPedidos() throws SQLException {
        List<Pedido> esperados = List.of(pedidoGuardado("REGISTRADO"));
        when(pedidoRepository.findAllByOrderByFechaHoraEntregaAsc()).thenReturn(esperados);

        List<Pedido> resultado = service.listar(null, null);

        assertEquals(esperados, resultado);
    }

    @Test
    void listar_conEstadoEnMinusculas_normalizaElEstado() throws SQLException {
        List<Pedido> esperados = List.of(pedidoGuardado("LISTO"));
        when(pedidoRepository.findByEstadoOrderByFechaHoraEntregaAsc("LISTO")).thenReturn(esperados);

        List<Pedido> resultado = service.listar("listo", null);

        assertEquals(esperados, resultado);
    }

    @Test
    void listar_conCliente_filtraPorCliente() throws SQLException {
        List<Pedido> esperados = List.of(pedidoGuardado("REGISTRADO"));
        when(pedidoRepository.findByIdClienteOrderByFechaHoraEntregaAsc(1)).thenReturn(esperados);

        List<Pedido> resultado = service.listar("", 1);

        assertEquals(esperados, resultado);
    }

    @Test
    void listar_conEstadoYCliente_filtraPorAmbos() throws SQLException {
        List<Pedido> esperados = List.of(pedidoGuardado("LISTO"));
        when(pedidoRepository.findByEstadoAndIdClienteOrderByFechaHoraEntregaAsc("LISTO", 1))
                .thenReturn(esperados);

        List<Pedido> resultado = service.listar("LISTO", 1);

        assertEquals(esperados, resultado);
    }

    @Test
    void listar_conEstadoInvalido_lanzaReglaNegocio() {
        assertThrows(ReglaNegocioException.class, () -> service.listar("INVENTADO", null));
    }

    // ---------- consultar ----------

    @Test
    void consultar_conIdExistente_devuelveElPedido() throws SQLException {
        Pedido pedido = pedidoGuardado("REGISTRADO");
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        Pedido resultado = service.consultar(1);

        assertSame(pedido, resultado);
    }

    @Test
    void consultar_conIdInexistente_lanzaNoEncontrado() {
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoEncontradoException.class, () -> service.consultar(99));
    }

    @Test
    void consultar_conIdNulo_lanzaNoEncontrado() {
        assertThrows(NoEncontradoException.class, () -> service.consultar(null));
    }

    @Test
    void consultar_conIdNoPositivo_lanzaNoEncontrado() {
        assertThrows(NoEncontradoException.class, () -> service.consultar(0));
    }

    // ---------- actualizarDatos ----------

    @Test
    void actualizarDatos_enEstadoRegistrado_actualizaLosCampos() throws SQLException {
        Pedido actual = pedidoGuardado("REGISTRADO");
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(actual));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        LocalDateTime nuevaFecha = LocalDateTime.now().plusDays(5);
        Pedido datos = Pedido.builder()
                .idPedido(1)
                .fechaHoraEntrega(nuevaFecha)
                .direccionEntrega("  Carrera 50 # 10-20  ")
                .ocasion("Cumpleaños")
                .build();

        Pedido resultado = service.actualizarDatos(datos);

        assertEquals("Carrera 50 # 10-20", resultado.getDireccionEntrega());
        assertEquals(nuevaFecha, resultado.getFechaHoraEntrega());
        assertEquals("Cumpleaños", resultado.getOcasion());
    }

    @Test
    void actualizarDatos_sinIdPedido_lanzaReglaNegocio() {
        Pedido datos = Pedido.builder().direccionEntrega("Calle 1").build();

        assertThrows(ReglaNegocioException.class, () -> service.actualizarDatos(datos));
    }

    @Test
    void actualizarDatos_datosNulos_lanzaReglaNegocio() {
        assertThrows(ReglaNegocioException.class, () -> service.actualizarDatos(null));
    }

    @Test
    void actualizarDatos_pedidoInexistente_lanzaNoEncontrado() {
        when(pedidoRepository.findById(1)).thenReturn(Optional.empty());
        Pedido datos = Pedido.builder().idPedido(1).build();

        assertThrows(NoEncontradoException.class, () -> service.actualizarDatos(datos));
    }

    @Test
    void actualizarDatos_siNoEstaRegistrado_lanzaReglaNegocioYNoGuarda() {
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedidoGuardado("LISTO")));
        Pedido datos = Pedido.builder()
                .idPedido(1)
                .fechaHoraEntrega(LocalDateTime.now().plusDays(3))
                .direccionEntrega("Calle 1")
                .build();

        assertThrows(ReglaNegocioException.class, () -> service.actualizarDatos(datos));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void actualizarDatos_conFechaPasada_lanzaReglaNegocio() {
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedidoGuardado("REGISTRADO")));
        Pedido datos = Pedido.builder()
                .idPedido(1)
                .fechaHoraEntrega(LocalDateTime.now().minusHours(1))
                .direccionEntrega("Calle 1")
                .build();

        assertThrows(ReglaNegocioException.class, () -> service.actualizarDatos(datos));
    }

    // ---------- cambiarEstado ----------

    @Test
    void cambiarEstado_conTransicionValida_actualizaElEstado() throws SQLException {
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedidoGuardado("REGISTRADO")));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = service.cambiarEstado(1, "en_elaboracion");

        assertEquals("EN_ELABORACION", resultado.getEstado());
        assertNull(resultado.getFechaEntregaReal());
    }

    @Test
    void cambiarEstado_aEntregado_registraLaFechaRealDeEntrega() throws SQLException {
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedidoGuardado("LISTO")));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = service.cambiarEstado(1, "ENTREGADO");

        assertEquals("ENTREGADO", resultado.getEstado());
        assertNotNull(resultado.getFechaEntregaReal());
    }

    @Test
    void cambiarEstado_conTransicionNoPermitida_lanzaReglaNegocioYNoGuarda() {
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedidoGuardado("REGISTRADO")));

        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(1, "ENTREGADO"));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void cambiarEstado_desdeEstadoFinal_lanzaReglaNegocio() {
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedidoGuardado("CANCELADO")));

        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(1, "REGISTRADO"));
    }

    @Test
    void cambiarEstado_conEstadoVacio_lanzaReglaNegocio() {
        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(1, "  "));
    }

    @Test
    void cambiarEstado_conEstadoDesconocido_lanzaReglaNegocio() {
        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(1, "INVENTADO"));
    }

    @Test
    void cambiarEstado_conPedidoInexistente_lanzaNoEncontrado() {
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoEncontradoException.class, () -> service.cambiarEstado(99, "LISTO"));
    }

    // ---------- guardarTarjeta ----------

    @Test
    void guardarTarjeta_sinTarjetaPrevia_creaLaTarjeta() throws SQLException {
        Pedido pedido = pedidoGuardado("REGISTRADO");
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        Tarjeta datos = Tarjeta.builder().mensaje("  Con cariño  ").remitente("  Ana  ").build();

        Pedido resultado = service.guardarTarjeta(1, datos);

        assertNotNull(resultado.getTarjeta());
        assertEquals("Con cariño", resultado.getTarjeta().getMensaje());
        assertEquals("Ana", resultado.getTarjeta().getRemitente());
        assertSame(resultado, resultado.getTarjeta().getPedido());
    }

    @Test
    void guardarTarjeta_conTarjetaPrevia_reemplazaElContenidoSinCrearOtra() throws SQLException {
        Pedido pedido = pedidoGuardado("REGISTRADO");
        Tarjeta existente = Tarjeta.builder().mensaje("Mensaje viejo").remitente("Carlos").build();
        pedido.asignarTarjeta(existente);
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = service.guardarTarjeta(1, Tarjeta.builder().mensaje("Mensaje nuevo").build());

        assertSame(existente, resultado.getTarjeta());
        assertEquals("Mensaje nuevo", resultado.getTarjeta().getMensaje());
        assertNull(resultado.getTarjeta().getRemitente());
    }

    @Test
    void guardarTarjeta_sinMensaje_lanzaReglaNegocio() {
        Tarjeta datos = Tarjeta.builder().mensaje("   ").build();

        assertThrows(ReglaNegocioException.class, () -> service.guardarTarjeta(1, datos));
    }

    @Test
    void guardarTarjeta_conTarjetaNula_lanzaReglaNegocio() {
        assertThrows(ReglaNegocioException.class, () -> service.guardarTarjeta(1, null));
    }

    @Test
    void guardarTarjeta_conMensajeMuyLargo_lanzaReglaNegocio() {
        Tarjeta datos = Tarjeta.builder().mensaje("a".repeat(201)).build();

        assertThrows(ReglaNegocioException.class, () -> service.guardarTarjeta(1, datos));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ENTREGADO", "CANCELADO"})
    void guardarTarjeta_enPedidoCerrado_lanzaReglaNegocioYNoGuarda(String estado) {
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedidoGuardado(estado)));
        Tarjeta datos = Tarjeta.builder().mensaje("Hola").build();

        assertThrows(ReglaNegocioException.class, () -> service.guardarTarjeta(1, datos));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void guardarTarjeta_conPedidoInexistente_lanzaNoEncontrado() {
        when(pedidoRepository.findById(99)).thenReturn(Optional.empty());
        Tarjeta datos = Tarjeta.builder().mensaje("Hola").build();

        assertThrows(NoEncontradoException.class, () -> service.guardarTarjeta(99, datos));
    }
}