package com.floristeria.bloom.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.floristeria.bloom.excepciones.ReglaNegocioException;
import com.floristeria.bloom.identidades.Pedido;
import com.floristeria.bloom.repositorios.IClienteRepository;
import com.floristeria.bloom.repositorios.IPedidoRepository;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private IPedidoRepository pedidoRepository;
    @Mock
    private IClienteRepository clienteRepository;
    @InjectMocks
    private PedidoService service;

    @Test
    void insertar_sinArreglos_lanzaReglaNegocio() {
        Pedido pedido = Pedido.builder().idCliente(1)
                .fechaHoraEntrega(LocalDateTime.now().plusDays(1))
                .direccionEntrega("Calle 1").arreglos(List.of()).build();

        assertThrows(ReglaNegocioException.class, () -> service.insertar(pedido));
    }

    @Test
    void cambiarEstado_saltandoUnEstado_lanzaReglaNegocio() throws SQLException {
        when(pedidoRepository.consultarPorId(1))
                .thenReturn(Pedido.builder().idPedido(1).estado("REGISTRADO").build());

        assertThrows(ReglaNegocioException.class, () -> service.cambiarEstado(1, "LISTO"));
    }
}