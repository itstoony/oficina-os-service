package br.com.fiap.os.atendimento.application.port.in;

import java.util.List;
import java.util.UUID;

public interface AtendenteUseCase {
    AtendenteDTO.Response cadastrar(AtendenteDTO.CadastrarRequest request);
    AtendenteDTO.Response atualizar(UUID id, AtendenteDTO.AtualizarRequest request);
    AtendenteDTO.Response buscarPorId(UUID id);
    List<AtendenteDTO.Response> listar();
    void deletar(UUID id);
}
