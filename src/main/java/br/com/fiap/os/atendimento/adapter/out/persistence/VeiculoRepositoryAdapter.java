package br.com.fiap.os.atendimento.adapter.out.persistence;

import br.com.fiap.os.atendimento.application.port.out.VeiculoRepositoryPort;
import br.com.fiap.os.atendimento.domain.model.Veiculo;
import br.com.fiap.os.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VeiculoRepositoryAdapter implements VeiculoRepositoryPort {

    private final VeiculoJpaRepository jpaRepository;

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        return jpaRepository.save(veiculo);
    }

    @Override
    public Veiculo buscarPorId(UUID id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado: " + id));
    }

    @Override
    public List<Veiculo> listar() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Veiculo> listarPorClienteId(UUID clienteId) {
        return jpaRepository.findByClienteId(clienteId);
    }

    @Override
    public void deletar(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByPlaca(String placa) {
        return jpaRepository.existsByPlacaValor(placa);
    }
}
