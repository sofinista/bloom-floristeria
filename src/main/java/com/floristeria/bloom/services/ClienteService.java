package com.floristeria.bloom.services;

import com.floristeria.bloom.excepciones.NoEncontradoException;
import com.floristeria.bloom.excepciones.ReglaNegocioException;
import com.floristeria.bloom.identidades.Cliente;
import com.floristeria.bloom.repositorios.IClienteRepository;
import com.floristeria.bloom.utilidades.Validaciones;

import java.sql.SQLException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClienteService implements IClienteService {

    private final IClienteRepository clienteRepository;

    @Override
    public List<Cliente> listar() throws SQLException {
        return clienteRepository.listar();
    }

    // Devuelve el cliente activo o lanza 404
    @Override
    public Cliente consultar(Integer id) throws SQLException {
        Cliente cliente = (id == null || id <= 0) ? null : clienteRepository.consultarPorId(id);
        if (cliente == null || !Boolean.TRUE.equals(cliente.getActivo())) {
            throw new NoEncontradoException("No existe un cliente activo con id " + id);
        }
        return cliente;
    }

    @Override
    public Cliente insertar(Cliente cliente) throws SQLException {
        validar(cliente);
        if (clienteRepository.existeDocumento(cliente.getNumeroDocumento(), 0)) {
            throw new ReglaNegocioException(
                    "Ya existe un cliente con el documento " + cliente.getNumeroDocumento());
        }
        return clienteRepository.insertar(cliente);
    }

    @Override
    public Cliente actualizar(Cliente cliente) throws SQLException {
        if (cliente == null || cliente.getIdCliente() == null) {
            throw new ReglaNegocioException("El idCliente es obligatorio para actualizar");
        }
        validar(cliente);
        consultar(cliente.getIdCliente()); // lanza 404 si no existe o esta inactivo
        if (clienteRepository.existeDocumento(cliente.getNumeroDocumento(), cliente.getIdCliente())) {
            throw new ReglaNegocioException(
                    "Otro cliente ya tiene el documento " + cliente.getNumeroDocumento());
        }
        clienteRepository.actualizar(cliente);
        return consultar(cliente.getIdCliente());
    }

    @Override
    public void eliminar(Integer id) throws SQLException {
        consultar(id); // lanza 404 si no existe o ya estaba inactivo
        clienteRepository.desactivar(id);
    }

    private void validar(Cliente cliente) {
        if (cliente == null) {
            throw new ReglaNegocioException("Debe enviar los datos del cliente");
        }
        if (Validaciones.vacio(cliente.getNombre())) {
            throw new ReglaNegocioException("El nombre es obligatorio");
        }
        if (Validaciones.vacio(cliente.getTelefono())) {
            throw new ReglaNegocioException("El telefono es obligatorio");
        }
        if (Validaciones.vacio(cliente.getTipoDocumento())) {
            throw new ReglaNegocioException("El tipoDocumento es obligatorio (CC, CE, PASAPORTE)");
        }
        if (Validaciones.vacio(cliente.getNumeroDocumento())) {
            throw new ReglaNegocioException("El numeroDocumento es obligatorio");
        }
        cliente.setNombre(cliente.getNombre().trim());
        cliente.setTelefono(cliente.getTelefono().trim());
        cliente.setTipoDocumento(cliente.getTipoDocumento().trim().toUpperCase());
        cliente.setNumeroDocumento(cliente.getNumeroDocumento().trim());
    }
}