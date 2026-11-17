package br.com.fiap.os.atendimento.adapter.out.persistence;

import br.com.fiap.os.atendimento.domain.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VeiculoJpaRepository extends JpaRepository<Veiculo, UUID> {
    List<Veiculo> findByClienteId(UUID clienteId);
    boolean existsByPlacaValor(String placaValor);
}
