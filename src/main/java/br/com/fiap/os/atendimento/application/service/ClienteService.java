package br.com.fiap.os.atendimento.application.service;

import br.com.fiap.os.atendimento.application.port.in.ClienteDTO;
import br.com.fiap.os.atendimento.application.port.in.ClienteUseCase;
import br.com.fiap.os.atendimento.application.port.out.ClienteRepositoryPort;
import br.com.fiap.os.atendimento.domain.model.Cliente;
import br.com.fiap.os.atendimento.domain.valueobject.Documento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService implements ClienteUseCase {

    private final ClienteRepositoryPort repositorio;

    @Override
    @Transactional
    public ClienteDTO.Response cadastrar(ClienteDTO.CadastrarRequest request) {
        Cliente cliente = Cliente.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .documento(Documento.of(request.documento()))
                .build();
        return toResponse(repositorio.salvar(cliente));
    }

    @Override
    @Transactional
    public ClienteDTO.Response atualizar(UUID id, ClienteDTO.AtualizarRequest request) {
        Cliente cliente = repositorio.buscarPorId(id);
        cliente.setNome(request.nome());
        cliente.setEmail(request.email());
        cliente.setTelefone(request.telefone());
        return toResponse(repositorio.salvar(cliente));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDTO.Response buscarPorId(UUID id) {
        return toResponse(repositorio.buscarPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDTO.Response> listar() {
        return repositorio.listar().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        repositorio.buscarPorId(id);
        repositorio.deletar(id);
    }

    private ClienteDTO.Response toResponse(Cliente cliente) {
        return new ClienteDTO.Response(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getDocumento() != null ? cliente.getDocumento().getFormatado() : null,
                cliente.getDocumento() != null ? cliente.getDocumento().getTipo().name() : null,
                cliente.getCriadoEm(),
                cliente.getAtualizadoEm()
        );
    }
}
