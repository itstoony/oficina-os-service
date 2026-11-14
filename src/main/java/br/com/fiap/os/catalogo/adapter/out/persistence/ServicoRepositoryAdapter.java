package br.com.fiap.os.catalogo.adapter.out.persistence;

import br.com.fiap.os.catalogo.application.port.out.ServicoRepositoryPort;
import br.com.fiap.os.catalogo.domain.model.Servico;
import br.com.fiap.os.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServicoRepositoryAdapter implements ServicoRepositoryPort {

    private final ServicoJpaRepository jpaRepository;

    @Override
    public Servico salvar(Servico servico) {
        return jpaRepository.save(servico);
    }

    @Override
    public Servico buscarPorId(UUID id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado: " + id));
    }

    @Override
    public List<Servico> listar() {
        return jpaRepository.findAll();
    }

    @Override
    public void deletar(UUID id) {
        jpaRepository.deleteById(id);
    }
}
