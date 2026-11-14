package br.com.fiap.os.atendimento.adapter.out.persistence;

import br.com.fiap.os.atendimento.domain.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClienteJpaRepository extends JpaRepository<Cliente, UUID> {
}
