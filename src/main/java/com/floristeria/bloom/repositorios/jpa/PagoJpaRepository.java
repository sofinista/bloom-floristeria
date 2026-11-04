package com.floristeria.bloom.repositorios.jpa;

import com.floristeria.bloom.identidades.Pago;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoJpaRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByIdPedidoOrderByFechaPagoAsc(Integer idPedido);

    @Query("SELECT COALESCE(SUM(p.monto), 0.0) FROM Pago p WHERE p.idPedido = :idPedido")
    Double totalPagado(@Param("idPedido") Integer idPedido);
}