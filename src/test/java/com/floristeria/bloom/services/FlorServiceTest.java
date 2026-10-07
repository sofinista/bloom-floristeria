package com.floristeria.bloom.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.floristeria.bloom.excepciones.ReglaNegocioException;
import com.floristeria.bloom.identidades.Flor;
import com.floristeria.bloom.repositorios.IFlorRepository;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlorServiceTest {

    @Mock
    private IFlorRepository repository;

    @InjectMocks
    private FlorService service;

    @Test
    void ajustarStock_dejandoStockNegativo_lanzaReglaNegocio() throws SQLException {
        when(repository.consultarPorId(1)).thenReturn(Flor.builder().idFlor(1).activo(true).build());
        when(repository.ajustarStock(1, -5)).thenReturn(false);
        assertThrows(ReglaNegocioException.class, () -> service.ajustarStock(1, -5));
    }

    @Test
    void ajustarStock_conCantidadCero_lanzaReglaNegocio() {
        assertThrows(ReglaNegocioException.class, () -> service.ajustarStock(1, 0));
    }
}