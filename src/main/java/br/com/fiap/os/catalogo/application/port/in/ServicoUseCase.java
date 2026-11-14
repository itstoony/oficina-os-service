package br.com.fiap.os.catalogo.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ServicoUseCase {
    ServicoDTO.Response criar(ServicoDTO.CriarRequest request);
    ServicoDTO.Response atualizar(UUID id, ServicoDTO.AtualizarRequest request);
    ServicoDTO.Response buscarPorId(UUID id);
    List<ServicoDTO.Response> listar();
    void deletar(UUID id);
}
