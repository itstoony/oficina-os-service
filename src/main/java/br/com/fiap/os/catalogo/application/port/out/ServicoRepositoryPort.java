package br.com.fiap.os.catalogo.application.port.out;

import br.com.fiap.os.catalogo.domain.model.Servico;

import java.util.List;
import java.util.UUID;

public interface ServicoRepositoryPort {
    Servico salvar(Servico servico);
    Servico buscarPorId(UUID id);
    List<Servico> listar();
    void deletar(UUID id);
}
