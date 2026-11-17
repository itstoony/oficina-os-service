package br.com.fiap.os.os.application.service;

import br.com.fiap.os.atendimento.application.port.out.AtendenteRepositoryPort;
import br.com.fiap.os.atendimento.application.port.out.ClienteRepositoryPort;
import br.com.fiap.os.atendimento.application.port.out.VeiculoRepositoryPort;
import br.com.fiap.os.atendimento.domain.model.Atendente;
import br.com.fiap.os.atendimento.domain.model.Cliente;
import br.com.fiap.os.atendimento.domain.model.Veiculo;
import br.com.fiap.os.catalogo.application.port.out.ServicoRepositoryPort;
import br.com.fiap.os.catalogo.domain.model.Servico;
import br.com.fiap.os.os.application.port.in.ItemPecaDTO;
import br.com.fiap.os.os.application.port.in.ItemServicoDTO;
import br.com.fiap.os.os.application.port.in.OrdemDeServicoAdminUseCase;
import br.com.fiap.os.os.application.port.in.OrdemDeServicoDTO;
import br.com.fiap.os.os.application.port.in.OrdemDeServicoPublicUseCase;
import br.com.fiap.os.os.application.port.out.NotificacaoEmailPort;
import br.com.fiap.os.os.application.port.out.OrdemDeServicoRepositoryPort;
import br.com.fiap.os.os.application.port.out.OsEventPublisherPort;
import br.com.fiap.os.os.domain.model.ItemPeca;
import br.com.fiap.os.os.domain.model.ItemServico;
import br.com.fiap.os.os.domain.model.OrdemDeServico;
import br.com.fiap.os.os.domain.valueobject.StatusOS;
import br.com.fiap.os.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdemDeServicoService implements OrdemDeServicoAdminUseCase, OrdemDeServicoPublicUseCase {

    private final OrdemDeServicoRepositoryPort repositorio;
    private final ClienteRepositoryPort clienteRepositorio;
    private final VeiculoRepositoryPort veiculoRepositorio;
    private final AtendenteRepositoryPort atendenteRepositorio;
    private final ServicoRepositoryPort servicoRepositorio;
    private final OsEventPublisherPort eventPublisher;
    private final NotificacaoEmailPort notificacaoEmail;

    @Override
    @Transactional(readOnly = true)
    public List<OrdemDeServicoDTO.Response> listar() {
        return repositorio.listarAtivasOrdenadas().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrdemDeServicoDTO.Response buscarPorId(UUID id) {
        return toResponse(repositorio.buscarPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrdemDeServicoDTO.StatusPublicoResponse buscarStatusPublico(String numero) {
        return toStatusPublicoResponse(repositorio.buscarPorNumero(numero));
    }

    @Override
    @Transactional
    public OrdemDeServicoDTO.Response criar(OrdemDeServicoDTO.CriarRequest request) {
        Cliente cliente = clienteRepositorio.buscarPorId(request.clienteId());
        Veiculo veiculo = veiculoRepositorio.buscarPorId(request.veiculoId());

        Atendente atendente = null;
        if (request.atendenteId() != null) {
            atendente = atendenteRepositorio.buscarPorId(request.atendenteId());
        }

        OrdemDeServico ordemDeServico = OrdemDeServico.builder()
                .numero(gerarNumero())
                .status(StatusOS.RECEBIDA)
                .cliente(cliente)
                .veiculo(veiculo)
                .atendente(atendente)
                .observacoes(request.observacoes())
                .build();

        OrdemDeServico salva = repositorio.salvar(ordemDeServico);
        publicarEventoSeguro(() -> eventPublisher.publicarOsAberta(salva), salva.getNumero(), "os.aberta");
        return toResponse(salva);
    }

    @Override
    @Transactional
    public OrdemDeServicoDTO.Response adicionarServico(UUID osId, ItemServicoDTO.AdicionarRequest request) {
        OrdemDeServico ordemDeServico = repositorio.buscarPorId(osId);
        ordemDeServico.validarEdicaoPermitida();

        Servico servico = servicoRepositorio.buscarPorId(request.servicoId());

        ItemServico itemServico = ItemServico.builder()
                .ordemDeServico(ordemDeServico)
                .servico(servico)
                .quantidade(request.quantidade())
                .precoUnitario(servico.getPrecoBase())
                .observacao(request.observacao())
                .build();

        ordemDeServico.getItensServico().add(itemServico);
        ordemDeServico.recalcularValorTotal();
        return toResponse(repositorio.salvar(ordemDeServico));
    }

    @Override
    @Transactional
    public OrdemDeServicoDTO.Response removerServico(UUID osId, UUID itemId) {
        OrdemDeServico ordemDeServico = repositorio.buscarPorId(osId);
        ordemDeServico.validarEdicaoPermitida();

        boolean removido = ordemDeServico.getItensServico().removeIf(item -> item.getId().equals(itemId));
        if (!removido) {
            throw new RecursoNaoEncontradoException("Item de serviço não encontrado: " + itemId);
        }

        ordemDeServico.recalcularValorTotal();
        return toResponse(repositorio.salvar(ordemDeServico));
    }

    @Override
    @Transactional
    public OrdemDeServicoDTO.Response adicionarPeca(UUID osId, ItemPecaDTO.AdicionarRequest request) {
        OrdemDeServico ordemDeServico = repositorio.buscarPorId(osId);
        ordemDeServico.validarEdicaoPermitida();

        ItemPeca itemPeca = ItemPeca.builder()
                .ordemDeServico(ordemDeServico)
                .pecaId(request.pecaId())
                .nomePeca(request.nomePeca())
                .quantidade(request.quantidade())
                .precoUnitario(request.precoUnitario())
                .build();

        ordemDeServico.getItensPeca().add(itemPeca);
        ordemDeServico.recalcularValorTotal();
        return toResponse(repositorio.salvar(ordemDeServico));
    }

    @Override
    @Transactional
    public OrdemDeServicoDTO.Response removerPeca(UUID osId, UUID itemId) {
        OrdemDeServico ordemDeServico = repositorio.buscarPorId(osId);
        ordemDeServico.validarEdicaoPermitida();

        boolean removido = ordemDeServico.getItensPeca().removeIf(item -> item.getId().equals(itemId));
        if (!removido) {
            throw new RecursoNaoEncontradoException("Item de peça não encontrado: " + itemId);
        }

        ordemDeServico.recalcularValorTotal();
        return toResponse(repositorio.salvar(ordemDeServico));
    }

    @Override
    @Transactional
    public OrdemDeServicoDTO.Response iniciarDiagnostico(UUID id) {
        OrdemDeServico ordemDeServico = repositorio.buscarPorId(id);
        ordemDeServico.transicionarPara(StatusOS.EM_DIAGNOSTICO);
        return toResponse(repositorio.salvar(ordemDeServico));
    }

    @Override
    @Transactional
    public OrdemDeServicoDTO.Response enviarOrcamento(UUID id) {
        OrdemDeServico ordemDeServico = repositorio.buscarPorId(id);
        ordemDeServico.transicionarPara(StatusOS.AGUARDANDO_APROVACAO);
        OrdemDeServico salva = repositorio.salvar(ordemDeServico);
        publicarEventoSeguro(() -> eventPublisher.publicarOrcamentoSolicitado(salva), salva.getNumero(), "orcamento.solicitado");
        notificarEmailSeguro(() -> notificacaoEmail.enviarOrcamentoParaAprovacao(
                salva.getCliente().getEmail(), salva.getNumero(), salva.getValorTotal()));
        return toResponse(salva);
    }

    @Override
    @Transactional
    public OrdemDeServicoDTO.Response iniciarExecucao(UUID id, OrdemDeServicoDTO.IniciarExecucaoRequest request) {
        OrdemDeServico ordemDeServico = repositorio.buscarPorId(id);
        Atendente atendente = atendenteRepositorio.buscarPorId(request.atendenteId());
        ordemDeServico.setAtendente(atendente);
        ordemDeServico.transicionarPara(StatusOS.EM_EXECUCAO);
        return toResponse(repositorio.salvar(ordemDeServico));
    }

    @Override
    @Transactional
    public OrdemDeServicoDTO.Response finalizar(UUID id) {
        OrdemDeServico ordemDeServico = repositorio.buscarPorId(id);
        ordemDeServico.transicionarPara(StatusOS.FINALIZADA);
        return toResponse(repositorio.salvar(ordemDeServico));
    }

    @Override
    @Transactional
    public OrdemDeServicoDTO.Response entregar(UUID id) {
        OrdemDeServico ordemDeServico = repositorio.buscarPorId(id);
        ordemDeServico.transicionarPara(StatusOS.ENTREGUE);
        return toResponse(repositorio.salvar(ordemDeServico));
    }

    @Override
    @Transactional
    public OrdemDeServicoDTO.Response cancelar(UUID id) {
        OrdemDeServico ordemDeServico = repositorio.buscarPorId(id);
        ordemDeServico.transicionarPara(StatusOS.CANCELADA);
        OrdemDeServico salva = repositorio.salvar(ordemDeServico);
        publicarEventoSeguro(() -> eventPublisher.publicarOsCancelada(salva), salva.getNumero(), "os.cancelada");
        return toResponse(salva);
    }

    @Override
    @Transactional
    public OrdemDeServicoDTO.StatusPublicoResponse aprovar(String numero) {
        OrdemDeServico ordemDeServico = repositorio.buscarPorNumero(numero);
        ordemDeServico.transicionarPara(StatusOS.APROVADO);
        OrdemDeServico salva = repositorio.salvar(ordemDeServico);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publicarEventoSeguro(() -> eventPublisher.publicarOsAprovada(salva), salva.getNumero(), "os.aprovada");
                notificarEmailSeguro(() -> notificacaoEmail.enviarConfirmacaoAprovacao(
                        salva.getCliente().getEmail(), salva.getNumero()));
            }
        });
        return toStatusPublicoResponse(salva);
    }

    @Override
    @Transactional
    public OrdemDeServicoDTO.StatusPublicoResponse recusar(String numero) {
        OrdemDeServico ordemDeServico = repositorio.buscarPorNumero(numero);
        ordemDeServico.transicionarPara(StatusOS.CANCELADA);
        OrdemDeServico salva = repositorio.salvar(ordemDeServico);
        publicarEventoSeguro(() -> eventPublisher.publicarOsCancelada(salva), salva.getNumero(), "os.cancelada");
        notificarEmailSeguro(() -> notificacaoEmail.enviarConfirmacaoRecusa(
                salva.getCliente().getEmail(), salva.getNumero()));
        return toStatusPublicoResponse(salva);
    }

    private String gerarNumero() {
        int ano = LocalDateTime.now().getYear();
        String prefixo = "OS-" + ano + "-%";
        int proximo = repositorio.buscarProximoSequencial(prefixo) + 1;
        return String.format("OS-%d-%05d", ano, proximo);
    }

    private void publicarEventoSeguro(Runnable publicacao, String numeroOs, String routingKey) {
        try {
            publicacao.run();
        } catch (Exception e) {
            log.warn("Falha ao publicar evento {} para OS {}: {}", routingKey, numeroOs, e.getMessage());
        }
    }

    private void notificarEmailSeguro(Runnable notificacao) {
        try {
            notificacao.run();
        } catch (Exception e) {
            log.warn("Falha ao enviar notificação por email: {}", e.getMessage());
        }
    }

    private OrdemDeServicoDTO.StatusPublicoResponse toStatusPublicoResponse(OrdemDeServico ordemDeServico) {
        return new OrdemDeServicoDTO.StatusPublicoResponse(
                ordemDeServico.getNumero(),
                ordemDeServico.getStatus().name(),
                ordemDeServico.getValorTotal(),
                ordemDeServico.getDataAbertura(),
                ordemDeServico.getDataInicioExecucao(),
                ordemDeServico.getDataFimExecucao()
        );
    }

    private OrdemDeServicoDTO.Response toResponse(OrdemDeServico ordemDeServico) {
        List<ItemServicoDTO.Response> itensServico = ordemDeServico.getItensServico().stream()
                .map(item -> new ItemServicoDTO.Response(
                        item.getId(),
                        item.getServico().getId(),
                        item.getServico().getNome(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())),
                        item.getObservacao()))
                .toList();

        List<ItemPecaDTO.Response> itensPeca = ordemDeServico.getItensPeca().stream()
                .map(item -> new ItemPecaDTO.Response(
                        item.getId(),
                        item.getPecaId(),
                        item.getNomePeca(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()))))
                .toList();

        return new OrdemDeServicoDTO.Response(
                ordemDeServico.getId(),
                ordemDeServico.getNumero(),
                ordemDeServico.getStatus().name(),
                ordemDeServico.getCliente().getId(),
                ordemDeServico.getCliente().getNome(),
                ordemDeServico.getVeiculo().getId(),
                ordemDeServico.getVeiculo().getPlaca() != null ? ordemDeServico.getVeiculo().getPlaca().getValor() : null,
                ordemDeServico.getAtendente() != null ? ordemDeServico.getAtendente().getId() : null,
                ordemDeServico.getAtendente() != null ? ordemDeServico.getAtendente().getNome() : null,
                ordemDeServico.getValorTotal(),
                ordemDeServico.getObservacoes(),
                itensServico,
                itensPeca,
                ordemDeServico.getDataAbertura(),
                ordemDeServico.getDataInicioExecucao(),
                ordemDeServico.getDataFimExecucao(),
                ordemDeServico.getCriadoEm(),
                ordemDeServico.getAtualizadoEm()
        );
    }
}
