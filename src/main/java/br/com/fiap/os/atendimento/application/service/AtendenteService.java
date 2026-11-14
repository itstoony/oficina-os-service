package br.com.fiap.os.atendimento.application.service;

import br.com.fiap.os.atendimento.application.port.in.AtendenteDTO;
import br.com.fiap.os.atendimento.application.port.in.AtendenteUseCase;
import br.com.fiap.os.atendimento.application.port.out.AtendenteRepositoryPort;
import br.com.fiap.os.atendimento.domain.model.Atendente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AtendenteService implements AtendenteUseCase {

    private final AtendenteRepositoryPort repositorio;

    @Override
    @Transactional
    public AtendenteDTO.Response cadastrar(AtendenteDTO.CadastrarRequest request) {
        Atendente atendente = Atendente.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .build();
        return toResponse(repositorio.salvar(atendente));
    }

    @Override
    @Transactional
    public AtendenteDTO.Response atualizar(UUID id, AtendenteDTO.AtualizarRequest request) {
        Atendente atendente = repositorio.buscarPorId(id);
        atendente.setNome(request.nome());
        atendente.setEmail(request.email());
        atendente.setTelefone(request.telefone());
        return toResponse(repositorio.salvar(atendente));
    }

    @Override
    @Transactional(readOnly = true)
    public AtendenteDTO.Response buscarPorId(UUID id) {
        return toResponse(repositorio.buscarPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AtendenteDTO.Response> listar() {
        return repositorio.listar().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        repositorio.buscarPorId(id);
        repositorio.deletar(id);
    }

    private AtendenteDTO.Response toResponse(Atendente atendente) {
        return new AtendenteDTO.Response(
                atendente.getId(),
                atendente.getNome(),
                atendente.getEmail(),
                atendente.getTelefone(),
                atendente.getCriadoEm(),
                atendente.getAtualizadoEm()
        );
    }
}
