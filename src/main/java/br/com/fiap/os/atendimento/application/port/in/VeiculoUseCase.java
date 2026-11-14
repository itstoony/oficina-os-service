package br.com.fiap.os.atendimento.application.port.in;

import java.util.List;
import java.util.UUID;

public interface VeiculoUseCase {
    VeiculoDTO.Response cadastrar(VeiculoDTO.CadastrarRequest request);
    VeiculoDTO.Response atualizar(UUID id, VeiculoDTO.AtualizarRequest request);
    VeiculoDTO.Response buscarPorId(UUID id);
    List<VeiculoDTO.Response> listar();
    List<VeiculoDTO.Response> listarPorCliente(UUID clienteId);
    void deletar(UUID id);
}
