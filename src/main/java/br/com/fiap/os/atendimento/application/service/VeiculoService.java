package br.com.fiap.os.atendimento.application.service;

import br.com.fiap.os.atendimento.application.port.in.VeiculoDTO;
import br.com.fiap.os.atendimento.application.port.in.VeiculoUseCase;
import br.com.fiap.os.atendimento.application.port.out.ClienteRepositoryPort;
import br.com.fiap.os.atendimento.application.port.out.VeiculoRepositoryPort;
import br.com.fiap.os.atendimento.domain.model.Cliente;
import br.com.fiap.os.atendimento.domain.model.Veiculo;
import br.com.fiap.os.atendimento.domain.valueobject.Placa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VeiculoService implements VeiculoUseCase {

    private final VeiculoRepositoryPort repositorio;
    private final ClienteRepositoryPort clienteRepositorio;

    @Override
    @Transactional
    public VeiculoDTO.Response cadastrar(VeiculoDTO.CadastrarRequest request) {
        Cliente cliente = clienteRepositorio.buscarPorId(request.clienteId());
        Veiculo veiculo = Veiculo.builder()
                .marca(request.marca())
                .modelo(request.modelo())
                .ano(request.ano())
                .cor(request.cor())
                .placa(Placa.of(request.placa()))
                .cliente(cliente)
                .build();
        return toResponse(repositorio.salvar(veiculo));
    }

    @Override
    @Transactional
    public VeiculoDTO.Response atualizar(UUID id, VeiculoDTO.AtualizarRequest request) {
        Veiculo veiculo = repositorio.buscarPorId(id);
        veiculo.setMarca(request.marca());
        veiculo.setModelo(request.modelo());
        veiculo.setAno(request.ano());
        veiculo.setCor(request.cor());
        return toResponse(repositorio.salvar(veiculo));
    }

    @Override
    @Transactional(readOnly = true)
    public VeiculoDTO.Response buscarPorId(UUID id) {
        return toResponse(repositorio.buscarPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VeiculoDTO.Response> listar() {
        return repositorio.listar().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VeiculoDTO.Response> listarPorCliente(UUID clienteId) {
        return repositorio.listarPorClienteId(clienteId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        repositorio.buscarPorId(id);
        repositorio.deletar(id);
    }

    private VeiculoDTO.Response toResponse(Veiculo veiculo) {
        return new VeiculoDTO.Response(
                veiculo.getId(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getPlaca() != null ? veiculo.getPlaca().getValor() : null,
                veiculo.getCliente().getId(),
                veiculo.getCliente().getNome(),
                veiculo.getCriadoEm(),
                veiculo.getAtualizadoEm()
        );
    }
}
