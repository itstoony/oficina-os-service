package br.com.fiap.os.catalogo.adapter.out.persistence;

import br.com.fiap.os.catalogo.domain.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServicoJpaRepository extends JpaRepository<Servico, UUID> {
}
