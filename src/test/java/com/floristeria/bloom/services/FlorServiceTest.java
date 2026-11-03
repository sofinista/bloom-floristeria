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
import com.floristeria.bloom.identidades.Flor;
import com.floristeria.bloom.repositorios.IFlorRepository;
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
class FlorServiceTest {

    @Mock
    private IFlorRepository repository;

    @InjectMocks
    private FlorService service;

    // ---------- datos de apoyo ----------

    private Flor florValida() {
        return Flor.builder().nombre("Rosa").color("Roja")
                .stock(10).precioUnitario(2500.0).build();
    }

    private Flor florActiva(int id) {
        return Flor.builder().idFlor(id).nombre("Rosa").color("Roja")
                .stock(10).precioUnitario(2500.0).activo(true).build();
    }

    // ---------- listar ----------

    @Test
    void listar_devuelveLasFloresDelRepositorio() throws SQLException {
        List<Flor> esperadas = List.of(florActiva(1), florActiva(2));
        when(repository.listar()).thenReturn(esperadas);

        List<Flor> resultado = service.listar();

        assertEquals(2, resultado.size());
        assertSame(esperadas, resultado);
    }

    @Test
    void listar_sinFlores_devuelveListaVacia() throws SQLException {
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
    void consultar_conFlorActiva_laDevuelve() throws SQLException {
        when(repository.consultarPorId(1)).thenReturn(florActiva(1));

        Flor resultado = service.consultar(1);

        assertEquals("Rosa", resultado.getNombre());
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
    void consultar_siEstaInactiva_lanzaNoEncontrado() throws SQLException {
        Flor inactiva = Flor.builder().idFlor(5).activo(false).build();
        when(repository.consultarPorId(5)).thenReturn(inactiva);

        assertThrows(NoEncontradoException.class, () -> service.consultar(5));
    }

    // ---------- insertar ----------

    @Test
    void insertar_conDatosValidos_guardaYDevuelveLaFlor() throws SQLException {
        Flor flor = florValida();
        when(repository.insertar(flor)).thenReturn(flor);

        Flor resultado = service.insertar(flor);

        assertSame(flor, resultado);
        verify(repository).insertar(flor);
    }

    @Test
    void insertar_limpiaEspaciosDeNombreYColor() throws SQLException {
        Flor flor = florValida();
        flor.setNombre("  Rosa  ");
        flor.setColor("  Roja ");
        when(repository.insertar(flor)).thenReturn(flor);

        Flor resultado = service.insertar(flor);

        assertEquals("Rosa", resultado.getNombre());
        assertEquals("Roja", resultado.getColor());
    }

    @Test
    void insertar_conFlorNula_lanzaReglaNegocio() throws SQLException {
        assertThrows(ReglaNegocioException.class, () -> service.insertar(null));
        verify(repository, never()).insertar(org.mockito.ArgumentMatchers.any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void insertar_sinNombre_lanzaReglaNegocio(String nombreInvalido) throws SQLException {
        Flor flor = florValida();
        flor.setNombre(nombreInvalido);

        ReglaNegocioException error =
                assertThrows(ReglaNegocioException.class, () -> service.insertar(flor));

        assertEquals("El nombre es obligatorio", error.getMessage());
        verify(repository, never()).insertar(flor);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void insertar_sinColor_lanzaReglaNegocio(String colorInvalido) throws SQLException {
        Flor flor = florValida();
        flor.setColor(colorInvalido);

        ReglaNegocioException error =
                assertThrows(ReglaNegocioException.class, () -> service.insertar(flor));

        assertEquals("El color es obligatorio", error.getMessage());
        verify(repository, never()).insertar(flor);
    }

    @Test
    void insertar_sinPrecio_lanzaReglaNegocio() throws SQLException {
        Flor flor = florValida();
        flor.setPrecioUnitario(null);

        assertThrows(ReglaNegocioException.class, () -> service.insertar(flor));
        verify(repository, never()).insertar(flor);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -2500.0})
    void insertar_conPrecioNoPositivo_lanzaReglaNegocio(double precioInvalido) throws SQLException {
        Flor flor = florValida();
        flor.setPrecioUnitario(precioInvalido);

        assertThrows(ReglaNegocioException.class, () -> service.insertar(flor));
        verify(repository, never()).insertar(flor);
    }

    @Test
    void insertar_sinStock_lanzaReglaNegocio() throws SQLException {
        Flor flor = florValida();
        flor.setStock(null);

        assertThrows(ReglaNegocioException.class, () -> service.insertar(flor));
        verify(repository, never()).insertar(flor);
    }

    @Test
    void insertar_conStockNegativo_lanzaReglaNegocio() throws SQLException {
        Flor flor = florValida();
        flor.setStock(-1);

        assertThrows(ReglaNegocioException.class, () -> service.insertar(flor));
        verify(repository, never()).insertar(flor);
    }

    @Test
    void insertar_conStockCero_esValido() throws SQLException {
        Flor flor = florValida();
        flor.setStock(0);
        when(repository.insertar(flor)).thenReturn(flor);

        Flor resultado = service.insertar(flor);

        assertEquals(0, resultado.getStock());
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_conDatosValidos_actualizaYDevuelveLaFlorActual() throws SQLException {
        Flor cambios = florActiva(1);
        cambios.setNombre("Rosa blanca");
        when(repository.consultarPorId(1)).thenReturn(cambios);

        Flor resultado = service.actualizar(cambios);

        assertEquals("Rosa blanca", resultado.getNombre());
        verify(repository).actualizar(cambios);
    }

    @Test
    void actualizar_conFlorNula_lanzaReglaNegocio() throws SQLException {
        assertThrows(ReglaNegocioException.class, () -> service.actualizar(null));
        verify(repository, never()).actualizar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void actualizar_sinId_lanzaReglaNegocio() throws SQLException {
        Flor sinId = florValida();

        assertThrows(ReglaNegocioException.class, () -> service.actualizar(sinId));
        verify(repository, never()).actualizar(sinId);
    }

    @Test
    void actualizar_conDatosInvalidos_lanzaReglaNegocio() throws SQLException {
        Flor flor = florActiva(1);
        flor.setPrecioUnitario(0.0);

        assertThrows(ReglaNegocioException.class, () -> service.actualizar(flor));
        verify(repository, never()).actualizar(flor);
    }

    @Test
    void actualizar_siLaFlorNoExiste_lanzaNoEncontrado() throws SQLException {
        Flor flor = florActiva(99);
        when(repository.consultarPorId(99)).thenReturn(null);

        assertThrows(NoEncontradoException.class, () -> service.actualizar(flor));
        verify(repository, never()).actualizar(flor);
    }

    // ---------- eliminar ----------

    @Test
    void eliminar_conFlorActiva_laDesactiva() throws SQLException {
        when(repository.consultarPorId(1)).thenReturn(florActiva(1));

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
    void eliminar_siYaEstabaInactiva_lanzaNoEncontrado() throws SQLException {
        Flor inactiva = Flor.builder().idFlor(5).activo(false).build();
        when(repository.consultarPorId(5)).thenReturn(inactiva);

        assertThrows(NoEncontradoException.class, () -> service.eliminar(5));
        verify(repository, never()).desactivar(5);
    }

    // ---------- ajustarStock ----------

    @Test
    void ajustarStock_conEntradaDeMercancia_devuelveLaFlor() throws SQLException {
        when(repository.consultarPorId(1)).thenReturn(florActiva(1));
        when(repository.ajustarStock(1, 5)).thenReturn(true);

        Flor resultado = service.ajustarStock(1, 5);

        assertEquals(1, resultado.getIdFlor());
        verify(repository).ajustarStock(1, 5);
    }

    @Test
    void ajustarStock_conSalidaPermitida_devuelveLaFlor() throws SQLException {
        when(repository.consultarPorId(1)).thenReturn(florActiva(1));
        when(repository.ajustarStock(1, -3)).thenReturn(true);

        Flor resultado = service.ajustarStock(1, -3);

        assertEquals(1, resultado.getIdFlor());
    }

    @Test
    void ajustarStock_conCantidadCero_lanzaReglaNegocio() throws SQLException {
        assertThrows(ReglaNegocioException.class, () -> service.ajustarStock(1, 0));
        verify(repository, never()).ajustarStock(1, 0);
    }

    @Test
    void ajustarStock_siLaFlorNoExiste_lanzaNoEncontrado() throws SQLException {
        when(repository.consultarPorId(99)).thenReturn(null);

        assertThrows(NoEncontradoException.class, () -> service.ajustarStock(99, 5));
        verify(repository, never()).ajustarStock(99, 5);
    }

    @Test
    void ajustarStock_dejandoStockNegativo_lanzaReglaNegocio() throws SQLException {
        when(repository.consultarPorId(1)).thenReturn(florActiva(1));
        when(repository.ajustarStock(1, -50)).thenReturn(false);

        ReglaNegocioException error =
                assertThrows(ReglaNegocioException.class, () -> service.ajustarStock(1, -50));

        assertEquals("Stock insuficiente: no puede quedar negativo", error.getMessage());
    }
}