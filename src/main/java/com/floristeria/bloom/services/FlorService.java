package com.floristeria.bloom.services;

import com.floristeria.bloom.excepciones.NoEncontradoException;
import com.floristeria.bloom.excepciones.ReglaNegocioException;
import com.floristeria.bloom.identidades.Flor;
import com.floristeria.bloom.repositorios.IFlorRepository;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlorService implements IFlorService {

    private final IFlorRepository repository;

    @Override
    public List<Flor> listar() throws SQLException {
        return repository.listar();
    }

    @Override
    public Flor consultar(Integer id) throws SQLException {
        Flor flor = (id == null || id <= 0) ? null : repository.consultarPorId(id);
        if (flor == null || !Boolean.TRUE.equals(flor.getActivo())) {
            throw new NoEncontradoException("No existe una flor activa con id " + id);
        }
        return flor;
    }

    @Override
    public Flor insertar(Flor flor) throws SQLException {
        validar(flor);
        if (flor.getStock() == null || flor.getStock() < 0) {
            throw new ReglaNegocioException("El stock inicial debe ser 0 o mayor");
        }
        return repository.insertar(flor);
    }

    @Override
    public Flor actualizar(Flor flor) throws SQLException {
        if (flor == null || flor.getIdFlor() == null) {
            throw new ReglaNegocioException("El idFlor es obligatorio para actualizar");
        }
        validar(flor);
        consultar(flor.getIdFlor());
        repository.actualizar(flor);
        return consultar(flor.getIdFlor());
    }

    @Override
    public void eliminar(Integer id) throws SQLException {
        consultar(id);
        repository.desactivar(id);
    }

    // cantidad positiva = entra mercancia; negativa = se usa o se pierde
    @Override
    public Flor ajustarStock(Integer id, int cantidad) throws SQLException {
        if (cantidad == 0) {
            throw new ReglaNegocioException("La cantidad no puede ser 0");
        }
        consultar(id);
        if (!repository.ajustarStock(id, cantidad)) {
            throw new ReglaNegocioException("Stock insuficiente: no puede quedar negativo");
        }
        return consultar(id);
    }

    private void validar(Flor flor) {
        if (flor == null || flor.getNombre() == null || flor.getNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre es obligatorio");
        }
        if (flor.getColor() == null || flor.getColor().isBlank()) {
            throw new ReglaNegocioException("El color es obligatorio");
        }
        if (flor.getPrecioUnitario() == null || flor.getPrecioUnitario() <= 0) {
            throw new ReglaNegocioException("El precioUnitario debe ser mayor a 0");
        }
        flor.setNombre(flor.getNombre().trim());
        flor.setColor(flor.getColor().trim());
    }
}