package com.floristeria.bloom.services;

import com.floristeria.bloom.excepciones.NoEncontradoException;
import com.floristeria.bloom.excepciones.ReglaNegocioException;
import com.floristeria.bloom.identidades.Arreglo;
import com.floristeria.bloom.identidades.Cliente;
import com.floristeria.bloom.identidades.Pedido;
import com.floristeria.bloom.identidades.Tarjeta;
import com.floristeria.bloom.repositorios.IClienteRepository;
import com.floristeria.bloom.repositorios.jpa.PedidoJpaRepository;
import com.floristeria.bloom.utilidades.Validaciones;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PedidoService implements IPedidoService {

    private static final String REGISTRADO = "REGISTRADO";
    private static final String ENTREGADO = "ENTREGADO";
    private static final String CANCELADO = "CANCELADO";
    private static final int MAXIMO_MENSAJE_TARJETA = 200;
    private static final Map<String, List<String>> TRANSICIONES = Map.of(
            REGISTRADO, List.of("EN_ELABORACION", CANCELADO),
            "EN_ELABORACION", List.of("LISTO", CANCELADO),
            "LISTO", List.of(ENTREGADO, CANCELADO),
            ENTREGADO, List.of(),
            CANCELADO, List.of());
    private static final String ESTADOS_VALIDOS =
            "Use: REGISTRADO, EN_ELABORACION, LISTO, ENTREGADO o CANCELADO";

    private final PedidoJpaRepository pedidoRepository;
    private final IClienteRepository clienteRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pedido insertar(Pedido pedido) throws SQLException {
        if (pedido == null) {
            throw new ReglaNegocioException("Debe enviar los datos del pedido");
        }
        if (pedido.getIdCliente() == null) {
            throw new ReglaNegocioException("El idCliente es obligatorio");
        }
        validarEntrega(pedido);
        validarArreglos(pedido.getArreglos());
        if (pedido.getTarjeta() != null) {
            validarTarjeta(pedido.getTarjeta());
        }
        validarClienteActivo(pedido.getIdCliente());
        return pedidoRepository.save(construirPedido(pedido));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listar(String estado, Integer idCliente) throws SQLException {
        String estadoFiltro = normalizarEstado(estado);
        if (estadoFiltro != null && idCliente != null) {
            return pedidoRepository.findByEstadoAndIdClienteOrderByFechaHoraEntregaAsc(estadoFiltro, idCliente);
        }
        if (estadoFiltro != null) {
            return pedidoRepository.findByEstadoOrderByFechaHoraEntregaAsc(estadoFiltro);
        }
        if (idCliente != null) {
            return pedidoRepository.findByIdClienteOrderByFechaHoraEntregaAsc(idCliente);
        }
        return pedidoRepository.findAllByOrderByFechaHoraEntregaAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido consultar(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new NoEncontradoException("No existe un pedido con id " + id);
        }
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe un pedido con id " + id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pedido actualizarDatos(Pedido datos) throws SQLException {
        if (datos == null || datos.getIdPedido() == null) {
            throw new ReglaNegocioException("El idPedido es obligatorio para actualizar");
        }
        Pedido actual = consultar(datos.getIdPedido());
        if (!REGISTRADO.equals(actual.getEstado())) {
            throw new ReglaNegocioException(
                    "Solo se puede editar un pedido en estado REGISTRADO (este esta " + actual.getEstado() + ")");
        }
        validarEntrega(datos);
        actual.setFechaHoraEntrega(datos.getFechaHoraEntrega());
        actual.setDireccionEntrega(datos.getDireccionEntrega().trim());
        actual.setOcasion(datos.getOcasion());
        return pedidoRepository.save(actual);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pedido cambiarEstado(Integer id, String nuevoEstado) throws SQLException {
        if (Validaciones.vacio(nuevoEstado)) {
            throw new ReglaNegocioException("El estado es obligatorio. " + ESTADOS_VALIDOS);
        }
        String destino = normalizarEstado(nuevoEstado);
        Pedido pedido = consultar(id);
        List<String> permitidos = TRANSICIONES.get(pedido.getEstado());
        if (!permitidos.contains(destino)) {
            throw new ReglaNegocioException("No se puede pasar de " + pedido.getEstado() + " a " + destino
                    + ". Desde " + pedido.getEstado() + " solo se permite: " + permitidos);
        }
        pedido.setEstado(destino);
        pedido.setFechaEntregaReal(ENTREGADO.equals(destino) ? LocalDateTime.now() : null);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pedido guardarTarjeta(Integer idPedido, Tarjeta datos) throws SQLException {
        validarTarjeta(datos);
        Pedido pedido = consultar(idPedido);
        if (ENTREGADO.equals(pedido.getEstado()) || CANCELADO.equals(pedido.getEstado())) {
            throw new ReglaNegocioException("No se puede cambiar la tarjeta de un pedido " + pedido.getEstado());
        }
        Tarjeta tarjeta = pedido.getTarjeta();
        if (tarjeta == null) {
            tarjeta = new Tarjeta();
            pedido.asignarTarjeta(tarjeta);
        }
        tarjeta.setMensaje(datos.getMensaje().trim());
        tarjeta.setRemitente(limpiar(datos.getRemitente()));
        return pedidoRepository.save(pedido);
    }

    // validaciones y utilidades privadas

    private void validarEntrega(Pedido pedido) {
        if (pedido.getFechaHoraEntrega() == null
                || !pedido.getFechaHoraEntrega().isAfter(LocalDateTime.now())) {
            throw new ReglaNegocioException("La fechaHoraEntrega es obligatoria y debe ser futura");
        }
        if (Validaciones.vacio(pedido.getDireccionEntrega())) {
            throw new ReglaNegocioException("La direccionEntrega es obligatoria");
        }
    }

    private void validarArreglos(List<Arreglo> arreglos) {
        if (arreglos == null || arreglos.isEmpty()) {
            throw new ReglaNegocioException("El pedido debe tener al menos un arreglo");
        }
        for (Arreglo arreglo : arreglos) {
            if (arreglo == null || Validaciones.vacio(arreglo.getTipoArreglo())) {
                throw new ReglaNegocioException("Cada arreglo debe tener tipoArreglo");
            }
            if (arreglo.getValorUnitario() == null || arreglo.getValorUnitario() <= 0) {
                throw new ReglaNegocioException("Cada arreglo debe tener un valorUnitario mayor a 0");
            }
        }
    }

    private void validarTarjeta(Tarjeta tarjeta) {
        if (tarjeta == null || Validaciones.vacio(tarjeta.getMensaje())) {
            throw new ReglaNegocioException("El mensaje de la tarjeta es obligatorio");
        }
        if (tarjeta.getMensaje().trim().length() > MAXIMO_MENSAJE_TARJETA) {
            throw new ReglaNegocioException(
                    "El mensaje de la tarjeta no puede superar los " + MAXIMO_MENSAJE_TARJETA + " caracteres");
        }
    }

    private void validarClienteActivo(Integer idCliente) throws SQLException {
        Cliente cliente = clienteRepository.consultarPorId(idCliente);
        if (cliente == null || !Boolean.TRUE.equals(cliente.getActivo())) {
            throw new ReglaNegocioException("El cliente no existe o esta inactivo");
        }
    }

    // Devuelve el estado en mayúsculas, null si viene vacío, o lanza error si no existe
    private String normalizarEstado(String estado) {
        if (Validaciones.vacio(estado)) {
            return null;
        }
        String normalizado = estado.trim().toUpperCase();
        if (!TRANSICIONES.containsKey(normalizado)) {
            throw new ReglaNegocioException("Estado no valido. " + ESTADOS_VALIDOS);
        }
        return normalizado;
    }

    private Pedido construirPedido(Pedido datos) {
        Pedido nuevo = Pedido.builder()
                .idCliente(datos.getIdCliente())
                .fechaHoraEntrega(datos.getFechaHoraEntrega())
                .direccionEntrega(datos.getDireccionEntrega().trim())
                .ocasion(datos.getOcasion())
                .estado(REGISTRADO)
                .build();
        double total = 0;
        for (Arreglo recibido : datos.getArreglos()) {
            nuevo.agregarArreglo(Arreglo.builder()
                    .tipoArreglo(recibido.getTipoArreglo().trim())
                    .tamano(recibido.getTamano())
                    .colores(recibido.getColores())
                    .descripcion(recibido.getDescripcion())
                    .valorUnitario(recibido.getValorUnitario())
                    .build());
            total += recibido.getValorUnitario();
        }
        nuevo.setValorTotal(Validaciones.redondear(total));
        if (datos.getTarjeta() != null) {
            nuevo.asignarTarjeta(Tarjeta.builder()
                    .mensaje(datos.getTarjeta().getMensaje().trim())
                    .remitente(limpiar(datos.getTarjeta().getRemitente()))
                    .build());
        }
        return nuevo;
    }

    private String limpiar(String texto) {
        return texto == null ? null : texto.trim();
    }
}