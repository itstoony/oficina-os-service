package br.com.fiap.os.atendimento.application.port.out;

import br.com.fiap.os.atendimento.domain.model.Veiculo;

import java.util.List;
import java.util.UUID;

public interface VeiculoRepositoryPort {
    Veiculo salvar(Veiculo veiculo);
    Veiculo buscarPorId(UUID id);
    List<Veiculo> listar();
    List<Veiculo> listarPorClienteId(UUID clienteId);
    void deletar(UUID id);
    boolean existsByPlaca(String placa);
}
