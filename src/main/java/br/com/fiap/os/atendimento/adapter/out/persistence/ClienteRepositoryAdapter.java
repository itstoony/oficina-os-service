package br.com.fiap.os.atendimento.adapter.out.persistence;

import br.com.fiap.os.atendimento.application.port.out.ClienteRepositoryPort;
import br.com.fiap.os.atendimento.domain.model.Cliente;
import br.com.fiap.os.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

    private final ClienteJpaRepository jpaRepository;

    @Override
    public Cliente salvar(Cliente cliente) {
        return jpaRepository.save(cliente);
    }

    @Override
    public Cliente buscarPorId(UUID id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + id));
    }

    @Override
    public List<Cliente> listar() {
        return jpaRepository.findAll();
    }

    @Override
    public void deletar(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByDocumentoNumero(String documentoNumero) {
        return jpaRepository.existsByDocumentoNumero(documentoNumero);
    }
}
