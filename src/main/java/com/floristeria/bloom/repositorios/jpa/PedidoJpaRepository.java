package com.floristeria.bloom.repositorios.jpa;

import com.floristeria.bloom.identidades.Pedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoJpaRepository extends JpaRepository<Pedido, Integer> {

    @Override
    @EntityGraph(attributePaths = {"arreglos", "tarjeta"})
    Optional<Pedido> findById(Integer id);

    @EntityGraph(attributePaths = {"arreglos", "tarjeta"})
    List<Pedido> findAllByOrderByFechaHoraEntregaAsc();

    @EntityGraph(attributePaths = {"arreglos", "tarjeta"})
    List<Pedido> findByEstadoOrderByFechaHoraEntregaAsc(String estado);

    @EntityGraph(attributePaths = {"arreglos", "tarjeta"})
    List<Pedido> findByIdClienteOrderByFechaHoraEntregaAsc(Integer idCliente);

    @EntityGraph(attributePaths = {"arreglos", "tarjeta"})
    List<Pedido> findByEstadoAndIdClienteOrderByFechaHoraEntregaAsc(String estado, Integer idCliente);
}