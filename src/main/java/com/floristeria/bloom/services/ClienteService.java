package com.floristeria.bloom.services;

import com.floristeria.bloom.excepciones.NoEncontradoException;
import com.floristeria.bloom.excepciones.ReglaNegocioException;
import com.floristeria.bloom.identidades.Cliente;
import com.floristeria.bloom.repositorios.ClienteRepository;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;

    public List<Cliente> listar() throws SQLException {
        return repository.listar();
    }

    // Devuelve el cliente activo o lanza 404
    public Cliente consultar(Integer id) throws SQLException {
        Cliente cliente = (id == null || id <= 0) ? null : repository.consultarPorId(id);
        if (cliente == null || !Boolean.TRUE.equals(cliente.getActivo())) {
            throw new NoEncontradoException("No existe un cliente activo con id " + id);
        }
        return cliente;
    }

    public Cliente insertar(Cliente cliente) throws SQLException {
        validar(cliente);
        if (repository.existeDocumento(cliente.getNumeroDocumento(), 0)) {
            throw new ReglaNegocioException(
                    "Ya existe un cliente con el documento " + cliente.getNumeroDocumento());
        }
        return repository.insertar(cliente);
    }

    public Cliente actualizar(Cliente cliente) throws SQLException {
        if (cliente == null || cliente.getIdCliente() == null) {
            throw new ReglaNegocioException("El idCliente es obligatorio para actualizar");
        }
        validar(cliente);
        consultar(cliente.getIdCliente()); // lanza 404 si no existe o esta inactivo
        if (repository.existeDocumento(cliente.getNumeroDocumento(), cliente.getIdCliente())) {
            throw new ReglaNegocioException(
                    "Otro cliente ya tiene el documento " + cliente.getNumeroDocumento());
        }
        repository.actualizar(cliente);
        return consultar(cliente.getIdCliente());
    }

    public void eliminar(Integer id) throws SQLException {
        consultar(id); // lanza 404 si no existe o ya estaba inactivo
        repository.desactivar(id);
    }

    private void validar(Cliente cliente) {
        if (cliente == null) {
            throw new ReglaNegocioException("Debe enviar los datos del cliente");
        }
        if (vacio(cliente.getNombre())) {
            throw new ReglaNegocioException("El nombre es obligatorio");
        }
        if (vacio(cliente.getTelefono())) {
            throw new ReglaNegocioException("El telefono es obligatorio");
        }
        if (vacio(cliente.getTipoDocumento())) {
            throw new ReglaNegocioException("El tipoDocumento es obligatorio (CC, CE, PASAPORTE)");
        }
        if (vacio(cliente.getNumeroDocumento())) {
            throw new ReglaNegocioException("El numeroDocumento es obligatorio");
        }
        cliente.setNombre(cliente.getNombre().trim());
        cliente.setTelefono(cliente.getTelefono().trim());
        cliente.setTipoDocumento(cliente.getTipoDocumento().trim().toUpperCase());
        cliente.setNumeroDocumento(cliente.getNumeroDocumento().trim());
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}