package br.com.fiap.os.atendimento.application.port.out;

import br.com.fiap.os.atendimento.domain.model.Atendente;

import java.util.List;
import java.util.UUID;

public interface AtendenteRepositoryPort {
    Atendente salvar(Atendente atendente);
    Atendente buscarPorId(UUID id);
    List<Atendente> listar();
    void deletar(UUID id);
}
