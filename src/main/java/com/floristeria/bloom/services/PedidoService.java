package com.floristeria.bloom.services;

import com.floristeria.bloom.excepciones.NoEncontradoException;
import com.floristeria.bloom.excepciones.ReglaNegocioException;
import com.floristeria.bloom.identidades.Arreglo;
import com.floristeria.bloom.identidades.Cliente;
import com.floristeria.bloom.identidades.Pedido;
import com.floristeria.bloom.repositorios.ClienteRepository;
import com.floristeria.bloom.repositorios.PedidoRepository;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PedidoService {

    // Regla de negocio: a que estados se puede pasar desde cada estado
    private static final Map<String, List<String>> TRANSICIONES = Map.of(
            "REGISTRADO", List.of("EN_ELABORACION", "CANCELADO"),
            "EN_ELABORACION", List.of("LISTO", "CANCELADO"),
            "LISTO", List.of("ENTREGADO", "CANCELADO"),
            "ENTREGADO", List.of(),
            "CANCELADO", List.of());

    private static final String ESTADOS_VALIDOS =
            "Use: REGISTRADO, EN_ELABORACION, LISTO, ENTREGADO o CANCELADO";

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // Registra un pedido con sus arreglos. El valor total lo calcula el sistema.
    public Pedido insertar(Pedido pedido) throws SQLException {
        if (pedido == null) {
            throw new ReglaNegocioException("Debe enviar los datos del pedido");
        }
        if (pedido.getIdCliente() == null) {
            throw new ReglaNegocioException("El idCliente es obligatorio");
        }
        validarEntrega(pedido);
        if (pedido.getArreglos() == null || pedido.getArreglos().isEmpty()) {
            throw new ReglaNegocioException("El pedido debe tener al menos un arreglo");
        }

        double total = 0;
        for (Arreglo arreglo : pedido.getArreglos()) {
            if (arreglo == null || vacio(arreglo.getTipoArreglo())) {
                throw new ReglaNegocioException("Cada arreglo debe tener tipoArreglo");
            }
            if (arreglo.getValorUnitario() == null || arreglo.getValorUnitario() <= 0) {
                throw new ReglaNegocioException("Cada arreglo debe tener un valorUnitario mayor a 0");
            }
            total += arreglo.getValorUnitario();
        }

        Cliente cliente = clienteRepository.consultarPorId(pedido.getIdCliente());
        if (cliente == null || !Boolean.TRUE.equals(cliente.getActivo())) {
            throw new ReglaNegocioException("El cliente no existe o esta inactivo");
        }

        pedido.setEstado("REGISTRADO");
        pedido.setValorTotal(Math.round(total * 100.0) / 100.0);
        Pedido guardado = pedidoRepository.insertarConArreglos(pedido);
        return pedidoRepository.consultarPorId(guardado.getIdPedido());
    }

    // Filtros opcionales: estado e idCliente
    public List<Pedido> listar(String estado, Integer idCliente) throws SQLException {
        String estadoFiltro = null;
        if (!vacio(estado)) {
            estadoFiltro = estado.trim().toUpperCase();
            if (!TRANSICIONES.containsKey(estadoFiltro)) {
                throw new ReglaNegocioException("Estado no valido. " + ESTADOS_VALIDOS);
            }
        }
        return pedidoRepository.listar(estadoFiltro, idCliente);
    }

    public Pedido consultar(Integer id) throws SQLException {
        Pedido pedido = (id == null || id <= 0) ? null : pedidoRepository.consultarPorId(id);
        if (pedido == null) {
            throw new NoEncontradoException("No existe un pedido con id " + id);
        }
        return pedido;
    }

    // Solo se puede editar mientras el pedido siga REGISTRADO
    public Pedido actualizarDatos(Pedido datos) throws SQLException {
        if (datos == null || datos.getIdPedido() == null) {
            throw new ReglaNegocioException("El idPedido es obligatorio para actualizar");
        }
        Pedido actual = consultar(datos.getIdPedido());
        if (!"REGISTRADO".equals(actual.getEstado())) {
            throw new ReglaNegocioException(
                    "Solo se puede editar un pedido en estado REGISTRADO (este esta " + actual.getEstado() + ")");
        }
        validarEntrega(datos);
        pedidoRepository.actualizarDatos(datos);
        return consultar(datos.getIdPedido());
    }

    // Cambia el estado validando que la transicion este permitida
    public Pedido cambiarEstado(Integer id, String nuevoEstado) throws SQLException {
        if (vacio(nuevoEstado)) {
            throw new ReglaNegocioException("El estado es obligatorio. " + ESTADOS_VALIDOS);
        }
        String destino = nuevoEstado.trim().toUpperCase();
        if (!TRANSICIONES.containsKey(destino)) {
            throw new ReglaNegocioException("Estado no valido. " + ESTADOS_VALIDOS);
        }

        Pedido pedido = consultar(id);
        List<String> permitidos = TRANSICIONES.get(pedido.getEstado());
        if (!permitidos.contains(destino)) {
            throw new ReglaNegocioException("No se puede pasar de " + pedido.getEstado() + " a " + destino
                    + ". Desde " + pedido.getEstado() + " solo se permite: " + permitidos);
        }

        // Al entregar, se registra la fecha y hora reales de la entrega
        LocalDateTime fechaEntregaReal = "ENTREGADO".equals(destino) ? LocalDateTime.now() : null;
        pedidoRepository.actualizarEstado(id, destino, fechaEntregaReal);
        return consultar(id);
    }

    private void validarEntrega(Pedido pedido) {
        if (pedido.getFechaHoraEntrega() == null
                || !pedido.getFechaHoraEntrega().isAfter(LocalDateTime.now())) {
            throw new ReglaNegocioException("La fechaHoraEntrega es obligatoria y debe ser futura");
        }
        if (vacio(pedido.getDireccionEntrega())) {
            throw new ReglaNegocioException("La direccionEntrega es obligatoria");
        }
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}