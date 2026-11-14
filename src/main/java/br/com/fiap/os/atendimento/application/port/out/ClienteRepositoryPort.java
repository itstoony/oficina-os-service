package br.com.fiap.os.atendimento.application.port.out;

import br.com.fiap.os.atendimento.domain.model.Cliente;

import java.util.List;
import java.util.UUID;

public interface ClienteRepositoryPort {
    Cliente salvar(Cliente cliente);
    Cliente buscarPorId(UUID id);
    List<Cliente> listar();
    void deletar(UUID id);
}
