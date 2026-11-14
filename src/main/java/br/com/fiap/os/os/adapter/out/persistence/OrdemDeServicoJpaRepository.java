package br.com.fiap.os.os.adapter.out.persistence;

import br.com.fiap.os.os.domain.model.OrdemDeServico;
import br.com.fiap.os.os.domain.valueobject.StatusOS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemDeServicoJpaRepository extends JpaRepository<OrdemDeServico, UUID> {

    Optional<OrdemDeServico> findByNumero(String numero);

    List<OrdemDeServico> findByAtivoTrueOrderByDataAberturaDesc();

    @Query("SELECT COUNT(o) FROM OrdemDeServico o WHERE o.numero LIKE :prefixo")
    int findMaxSequencialByPrefixo(@Param("prefixo") String prefixo);

    @Query("SELECT o FROM OrdemDeServico o WHERE o.status IN :statuses AND o.dataInicioExecucao IS NOT NULL AND o.dataFimExecucao IS NOT NULL")
    List<OrdemDeServico> findFinalizadasComDatas(@Param("statuses") List<StatusOS> statuses);
}
