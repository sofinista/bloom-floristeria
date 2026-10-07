package com.floristeria.bloom.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.floristeria.bloom.excepciones.ReglaNegocioException;
import com.floristeria.bloom.identidades.Cliente;
import com.floristeria.bloom.repositorios.IClienteRepository;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private IClienteRepository repository;

    @InjectMocks
    private ClienteService service;

    @Test
    void insertar_conDocumentoRepetido_lanzaReglaNegocio() throws SQLException {
        Cliente cliente = Cliente.builder().nombre("Ana").telefono("300")
                .tipoDocumento("CC").numeroDocumento("123").build();
        when(repository.existeDocumento("123", 0)).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> service.insertar(cliente));
    }

    @Test
    void insertar_sinNombre_lanzaReglaNegocio() {
        Cliente cliente = Cliente.builder().telefono("300")
                .tipoDocumento("CC").numeroDocumento("123").build();

        assertThrows(ReglaNegocioException.class, () -> service.insertar(cliente));
    }
}