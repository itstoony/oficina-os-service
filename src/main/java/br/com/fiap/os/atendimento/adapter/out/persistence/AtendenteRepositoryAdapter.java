package br.com.fiap.os.atendimento.adapter.out.persistence;

import br.com.fiap.os.atendimento.application.port.out.AtendenteRepositoryPort;
import br.com.fiap.os.atendimento.domain.model.Atendente;
import br.com.fiap.os.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AtendenteRepositoryAdapter implements AtendenteRepositoryPort {

    private final AtendenteJpaRepository jpaRepository;

    @Override
    public Atendente salvar(Atendente atendente) {
        return jpaRepository.save(atendente);
    }

    @Override
    public Atendente buscarPorId(UUID id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendente não encontrado: " + id));
    }

    @Override
    public List<Atendente> listar() {
        return jpaRepository.findAll();
    }

    @Override
    public void deletar(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
