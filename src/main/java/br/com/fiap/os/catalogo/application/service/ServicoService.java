package br.com.fiap.os.catalogo.application.service;

import br.com.fiap.os.catalogo.application.port.in.ServicoDTO;
import br.com.fiap.os.catalogo.application.port.in.ServicoUseCase;
import br.com.fiap.os.catalogo.application.port.out.ServicoRepositoryPort;
import br.com.fiap.os.catalogo.domain.model.Servico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServicoService implements ServicoUseCase {

    private final ServicoRepositoryPort repositorio;

    @Override
    @Transactional
    public ServicoDTO.Response criar(ServicoDTO.CriarRequest request) {
        Servico servico = Servico.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .precoBase(request.precoBase())
                .build();
        return toResponse(repositorio.salvar(servico));
    }

    @Override
    @Transactional
    public ServicoDTO.Response atualizar(UUID id, ServicoDTO.AtualizarRequest request) {
        Servico servico = repositorio.buscarPorId(id);
        servico.setNome(request.nome());
        servico.setDescricao(request.descricao());
        servico.setPrecoBase(request.precoBase());
        return toResponse(repositorio.salvar(servico));
    }

    @Override
    @Transactional(readOnly = true)
    public ServicoDTO.Response buscarPorId(UUID id) {
        return toResponse(repositorio.buscarPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicoDTO.Response> listar() {
        return repositorio.listar().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        repositorio.buscarPorId(id);
        repositorio.deletar(id);
    }

    private ServicoDTO.Response toResponse(Servico servico) {
        return new ServicoDTO.Response(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getPrecoBase(),
                servico.getCriadoEm(),
                servico.getAtualizadoEm()
        );
    }
}
