package com.floristeria.bloom.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.floristeria.bloom.excepciones.NoEncontradoException;
import com.floristeria.bloom.excepciones.ReglaNegocioException;
import com.floristeria.bloom.identidades.Cliente;
import com.floristeria.bloom.repositorios.IClienteRepository;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private IClienteRepository repository;

    @InjectMocks
    private ClienteService service;

    // ---------- datos de apoyo ----------

    private Cliente clienteValido() {
        return Cliente.builder().nombre("Ana").telefono("300")
                .tipoDocumento("CC").numeroDocumento("123").build();
    }

    private Cliente clienteActivo(int id) {
        return Cliente.builder().idCliente(id).nombre("Ana").telefono("300")
                .tipoDocumento("CC").numeroDocumento("123").activo(true).build();
    }

    // ---------- listar ----------

    @Test
    void listar_devuelveLosClientesDelRepositorio() throws SQLException {
        List<Cliente> esperados = List.of(clienteActivo(1), clienteActivo(2));
        when(repository.listar()).thenReturn(esperados);

        List<Cliente> resultado = service.listar();

        assertEquals(2, resultado.size());
        assertSame(esperados, resultado);
    }

    @Test
    void listar_sinClientes_devuelveListaVacia() throws SQLException {
        when(repository.listar()).thenReturn(List.of());

        assertTrue(service.listar().isEmpty());
    }

    @Test
    void listar_cuandoElRepositorioFalla_propagaSQLException() throws SQLException {
        when(repository.listar()).thenThrow(new SQLException("fallo"));

        assertThrows(SQLException.class, () -> service.listar());
    }

    // ---------- consultar ----------

    @Test
    void consultar_conClienteActivo_loDevuelve() throws SQLException {
        when(repository.consultarPorId(1)).thenReturn(clienteActivo(1));

        Cliente resultado = service.consultar(1);

        assertEquals("Ana", resultado.getNombre());
    }

    @Test
    void consultar_conIdNulo_lanzaNoEncontrado() {
        assertThrows(NoEncontradoException.class, () -> service.consultar(null));
    }

    @Test
    void consultar_conIdCero_lanzaNoEncontrado() {
        assertThrows(NoEncontradoException.class, () -> service.consultar(0));
    }

    @Test
    void consultar_siNoExiste_lanzaNoEncontrado() throws SQLException {
        when(repository.consultarPorId(99)).thenReturn(null);

        assertThrows(NoEncontradoException.class, () -> service.consultar(99));
    }

    @Test
    void consultar_siEstaInactivo_lanzaNoEncontrado() throws SQLException {
        Cliente inactivo = Cliente.builder().idCliente(5).activo(false).build();
        when(repository.consultarPorId(5)).thenReturn(inactivo);

        assertThrows(NoEncontradoException.class, () -> service.consultar(5));
    }

    // ---------- insertar ----------

    @Test
    void insertar_conDatosValidos_guardaYDevuelveElCliente() throws SQLException {
        Cliente cliente = clienteValido();
        when(repository.existeDocumento("123", 0)).thenReturn(false);
        when(repository.insertar(cliente)).thenReturn(cliente);

        Cliente resultado = service.insertar(cliente);

        assertSame(cliente, resultado);
        verify(repository).insertar(cliente);
    }

    @Test
    void insertar_limpiaEspaciosYPasaElTipoDocumentoAMayusculas() throws SQLException {
        Cliente cliente = Cliente.builder().nombre("  Ana  ").telefono(" 300 ")
                .tipoDocumento(" cc ").numeroDocumento(" 123 ").build();
        when(repository.existeDocumento("123", 0)).thenReturn(false);
        when(repository.insertar(cliente)).thenReturn(cliente);

        Cliente resultado = service.insertar(cliente);

        assertEquals("Ana", resultado.getNombre());
        assertEquals("300", resultado.getTelefono());
        assertEquals("CC", resultado.getTipoDocumento());
        assertEquals("123", resultado.getNumeroDocumento());
    }

    @Test
    void insertar_conClienteNulo_lanzaReglaNegocio() throws SQLException {
        assertThrows(ReglaNegocioException.class, () -> service.insertar(null));
        verify(repository, never()).insertar(org.mockito.ArgumentMatchers.any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void insertar_sinNombre_lanzaReglaNegocio(String nombreInvalido) throws SQLException {
        Cliente cliente = clienteValido();
        cliente.setNombre(nombreInvalido);

        ReglaNegocioException error =
                assertThrows(ReglaNegocioException.class, () -> service.insertar(cliente));

        assertEquals("El nombre es obligatorio", error.getMessage());
        verify(repository, never()).insertar(cliente);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void insertar_sinTelefono_lanzaReglaNegocio(String telefonoInvalido) throws SQLException {
        Cliente cliente = clienteValido();
        cliente.setTelefono(telefonoInvalido);

        ReglaNegocioException error =
                assertThrows(ReglaNegocioException.class, () -> service.insertar(cliente));

        assertEquals("El telefono es obligatorio", error.getMessage());
        verify(repository, never()).insertar(cliente);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void insertar_sinTipoDocumento_lanzaReglaNegocio(String tipoInvalido) throws SQLException {
        Cliente cliente = clienteValido();
        cliente.setTipoDocumento(tipoInvalido);

        assertThrows(ReglaNegocioException.class, () -> service.insertar(cliente));
        verify(repository, never()).insertar(cliente);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void insertar_sinNumeroDocumento_lanzaReglaNegocio(String numeroInvalido) throws SQLException {
        Cliente cliente = clienteValido();
        cliente.setNumeroDocumento(numeroInvalido);

        assertThrows(ReglaNegocioException.class, () -> service.insertar(cliente));
        verify(repository, never()).insertar(cliente);
    }

    @Test
    void insertar_conDocumentoRepetido_lanzaReglaNegocio() throws SQLException {
        Cliente cliente = clienteValido();
        when(repository.existeDocumento("123", 0)).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> service.insertar(cliente));
        verify(repository, never()).insertar(cliente);
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_conDatosValidos_actualizaYDevuelveElClienteActual() throws SQLException {
        Cliente cambios = clienteActivo(1);
        cambios.setNombre("Ana Maria");
        when(repository.consultarPorId(1)).thenReturn(cambios);
        when(repository.existeDocumento("123", 1)).thenReturn(false);

        Cliente resultado = service.actualizar(cambios);

        assertEquals("Ana Maria", resultado.getNombre());
        verify(repository).actualizar(cambios);
    }

    @Test
    void actualizar_conClienteNulo_lanzaReglaNegocio() throws SQLException {
        assertThrows(ReglaNegocioException.class, () -> service.actualizar(null));
        verify(repository, never()).actualizar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void actualizar_sinId_lanzaReglaNegocio() throws SQLException {
        Cliente sinId = clienteValido();

        assertThrows(ReglaNegocioException.class, () -> service.actualizar(sinId));
        verify(repository, never()).actualizar(sinId);
    }

    @Test
    void actualizar_conDatosInvalidos_lanzaReglaNegocio() throws SQLException {
        Cliente cliente = clienteActivo(1);
        cliente.setNombre("");

        assertThrows(ReglaNegocioException.class, () -> service.actualizar(cliente));
        verify(repository, never()).actualizar(cliente);
    }

    @Test
    void actualizar_siElClienteNoExiste_lanzaNoEncontrado() throws SQLException {
        Cliente cliente = clienteActivo(99);
        when(repository.consultarPorId(99)).thenReturn(null);

        assertThrows(NoEncontradoException.class, () -> service.actualizar(cliente));
        verify(repository, never()).actualizar(cliente);
    }

    @Test
    void actualizar_conDocumentoDeOtroCliente_lanzaReglaNegocio() throws SQLException {
        Cliente cliente = clienteActivo(1);
        when(repository.consultarPorId(1)).thenReturn(cliente);
        when(repository.existeDocumento("123", 1)).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> service.actualizar(cliente));
        verify(repository, never()).actualizar(cliente);
    }

    // ---------- eliminar ----------

    @Test
    void eliminar_conClienteActivo_lo_desactiva() throws SQLException {
        when(repository.consultarPorId(1)).thenReturn(clienteActivo(1));

        service.eliminar(1);

        verify(repository).desactivar(1);
    }

    @Test
    void eliminar_siNoExiste_lanzaNoEncontrado() throws SQLException {
        when(repository.consultarPorId(99)).thenReturn(null);

        assertThrows(NoEncontradoException.class, () -> service.eliminar(99));
        verify(repository, never()).desactivar(99);
    }

    @Test
    void eliminar_siYaEstabaInactivo_lanzaNoEncontrado() throws SQLException {
        Cliente inactivo = Cliente.builder().idCliente(5).activo(false).build();
        when(repository.consultarPorId(5)).thenReturn(inactivo);

        assertThrows(NoEncontradoException.class, () -> service.eliminar(5));
        verify(repository, never()).desactivar(5);
    }
}