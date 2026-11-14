package br.com.fiap.os.atendimento.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ClienteUseCase {
    ClienteDTO.Response cadastrar(ClienteDTO.CadastrarRequest request);
    ClienteDTO.Response atualizar(UUID id, ClienteDTO.AtualizarRequest request);
    ClienteDTO.Response buscarPorId(UUID id);
    List<ClienteDTO.Response> listar();
    void deletar(UUID id);
}
