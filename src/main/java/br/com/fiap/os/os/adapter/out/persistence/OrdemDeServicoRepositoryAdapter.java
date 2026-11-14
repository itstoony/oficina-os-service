package br.com.fiap.os.os.adapter.out.persistence;

import br.com.fiap.os.os.application.port.out.OrdemDeServicoRepositoryPort;
import br.com.fiap.os.os.domain.model.OrdemDeServico;
import br.com.fiap.os.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrdemDeServicoRepositoryAdapter implements OrdemDeServicoRepositoryPort {

    private final OrdemDeServicoJpaRepository jpaRepository;

    @Override
    public OrdemDeServico salvar(OrdemDeServico ordemDeServico) {
        return jpaRepository.save(ordemDeServico);
    }

    @Override
    public OrdemDeServico buscarPorId(UUID id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada: " + id));
    }

    @Override
    public OrdemDeServico buscarPorNumero(String numero) {
        return jpaRepository.findByNumero(numero)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço não encontrada: " + numero));
    }

    @Override
    public List<OrdemDeServico> listarAtivasOrdenadas() {
        return jpaRepository.findByAtivoTrueOrderByDataAberturaDesc();
    }

    @Override
    public int buscarProximoSequencial(String prefixo) {
        return jpaRepository.findMaxSequencialByPrefixo(prefixo);
    }
}
