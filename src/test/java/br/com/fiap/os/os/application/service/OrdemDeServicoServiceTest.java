package br.com.fiap.os.os.application.service;

import br.com.fiap.os.atendimento.application.port.out.AtendenteRepositoryPort;
import br.com.fiap.os.atendimento.application.port.out.ClienteRepositoryPort;
import br.com.fiap.os.atendimento.application.port.out.VeiculoRepositoryPort;
import br.com.fiap.os.atendimento.domain.model.Atendente;
import br.com.fiap.os.atendimento.domain.model.Cliente;
import br.com.fiap.os.atendimento.domain.model.Veiculo;
import br.com.fiap.os.atendimento.domain.valueobject.Placa;
import br.com.fiap.os.catalogo.application.port.out.ServicoRepositoryPort;
import br.com.fiap.os.catalogo.domain.model.Servico;
import br.com.fiap.os.os.application.port.in.ItemPecaDTO;
import br.com.fiap.os.os.application.port.in.ItemServicoDTO;
import br.com.fiap.os.os.domain.model.ItemPeca;
import br.com.fiap.os.os.domain.model.ItemServico;
import br.com.fiap.os.os.application.port.in.OrdemDeServicoDTO;
import br.com.fiap.os.os.application.port.out.NotificacaoEmailPort;
import br.com.fiap.os.os.application.port.out.OrdemDeServicoRepositoryPort;
import br.com.fiap.os.os.application.port.out.OsEventPublisherPort;
import br.com.fiap.os.os.domain.model.OrdemDeServico;
import br.com.fiap.os.os.domain.valueobject.StatusOS;
import br.com.fiap.os.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemDeServicoServiceTest {

    @Mock private OrdemDeServicoRepositoryPort repositorio;
    @Mock private ClienteRepositoryPort clienteRepositorio;
    @Mock private VeiculoRepositoryPort veiculoRepositorio;
    @Mock private AtendenteRepositoryPort atendenteRepositorio;
    @Mock private ServicoRepositoryPort servicoRepositorio;
    @Mock private OsEventPublisherPort eventPublisher;
    @Mock private NotificacaoEmailPort notificacaoEmail;

    @InjectMocks
    private OrdemDeServicoService service;

    private Cliente cliente;
    private Veiculo veiculo;
    private OrdemDeServico ordemDeServico;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .id(UUID.randomUUID())
                .nome("João Silva")
                .email("joao@email.com")
                .telefone("11999999999")
                .build();

        veiculo = Veiculo.builder()
                .id(UUID.randomUUID())
                .marca("Toyota")
                .modelo("Corolla")
                .ano(2020)
                .cor("Prata")
                .placa(Placa.of("ABC1234"))
                .cliente(cliente)
                .build();

        ordemDeServico = OrdemDeServico.builder()
                .id(UUID.randomUUID())
                .numero("OS-2026-00001")
                .status(StatusOS.RECEBIDA)
                .cliente(cliente)
                .veiculo(veiculo)
                .valorTotal(BigDecimal.ZERO)
                .build();
    }

    @Test
    @DisplayName("Deve criar OS com status RECEBIDA")
    void deveCriarOsComStatusRecebida() {
        OrdemDeServicoDTO.CriarRequest request = new OrdemDeServicoDTO.CriarRequest(
                cliente.getId(), veiculo.getId(), null, null);

        when(clienteRepositorio.buscarPorId(cliente.getId())).thenReturn(cliente);
        when(veiculoRepositorio.buscarPorId(veiculo.getId())).thenReturn(veiculo);
        when(repositorio.buscarProximoSequencial(any())).thenReturn(0);
        when(repositorio.salvar(any())).thenReturn(ordemDeServico);
        doNothing().when(eventPublisher).publicarOsAberta(any());

        OrdemDeServicoDTO.Response response = service.criar(request);

        assertThat(response.status()).isEqualTo("RECEBIDA");
        verify(repositorio).salvar(any());
        verify(eventPublisher).publicarOsAberta(any());
    }

    @Test
    @DisplayName("Deve iniciar diagnóstico — RECEBIDA → EM_DIAGNOSTICO")
    void deveIniciarDiagnostico() {
        when(repositorio.buscarPorId(ordemDeServico.getId())).thenReturn(ordemDeServico);
        when(repositorio.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemDeServicoDTO.Response response = service.iniciarDiagnostico(ordemDeServico.getId());

        assertThat(response.status()).isEqualTo("EM_DIAGNOSTICO");
    }

    @Test
    @DisplayName("Deve cancelar OS")
    void deveCancelarOs() {
        when(repositorio.buscarPorId(ordemDeServico.getId())).thenReturn(ordemDeServico);
        when(repositorio.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(eventPublisher).publicarOsCancelada(any());

        OrdemDeServicoDTO.Response response = service.cancelar(ordemDeServico.getId());

        assertThat(response.status()).isEqualTo("CANCELADA");
    }

    @Test
    @DisplayName("Deve lançar exceção em transição inválida de status")
    void deveLancarExcecaoEmTransicaoInvalida() {
        when(repositorio.buscarPorId(ordemDeServico.getId())).thenReturn(ordemDeServico);

        assertThatThrownBy(() -> service.finalizar(ordemDeServico.getId()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("RECEBIDA");
    }

    @Test
    @DisplayName("Deve adicionar serviço à OS")
    void deveAdicionarServico() {
        Servico servico = Servico.builder()
                .id(UUID.randomUUID())
                .nome("Troca de óleo")
                .descricao("Troca de óleo e filtro")
                .precoBase(new BigDecimal("150.00"))
                .build();

        ItemServicoDTO.AdicionarRequest request = new ItemServicoDTO.AdicionarRequest(
                servico.getId(), 1, null);

        when(repositorio.buscarPorId(ordemDeServico.getId())).thenReturn(ordemDeServico);
        when(servicoRepositorio.buscarPorId(servico.getId())).thenReturn(servico);
        when(repositorio.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemDeServicoDTO.Response response = service.adicionarServico(ordemDeServico.getId(), request);

        assertThat(response.itensServico()).hasSize(1);
        assertThat(response.itensServico().get(0).nomeServico()).isEqualTo("Troca de óleo");
    }

    @Test
    @DisplayName("Deve listar somente OSs ativas")
    void deveListarOsAtivas() {
        when(repositorio.listarAtivasOrdenadas()).thenReturn(List.of(ordemDeServico));

        List<OrdemDeServicoDTO.Response> lista = service.listar();

        assertThat(lista).hasSize(1);
    }

    @Test
    @DisplayName("Deve buscar OS por id")
    void deveBuscarPorId() {
        when(repositorio.buscarPorId(ordemDeServico.getId())).thenReturn(ordemDeServico);

        OrdemDeServicoDTO.Response response = service.buscarPorId(ordemDeServico.getId());

        assertThat(response.numero()).isEqualTo("OS-2026-00001");
    }

    @Test
    @DisplayName("Deve buscar status público por número")
    void deveBuscarStatusPublico() {
        when(repositorio.buscarPorNumero(ordemDeServico.getNumero())).thenReturn(ordemDeServico);

        OrdemDeServicoDTO.StatusPublicoResponse response = service.buscarStatusPublico(ordemDeServico.getNumero());

        assertThat(response.status()).isEqualTo("RECEBIDA");
    }

    @Test
    @DisplayName("Deve enviar orçamento — EM_DIAGNOSTICO → AGUARDANDO_APROVACAO")
    void deveEnviarOrcamento() {
        ordemDeServico.transicionarPara(StatusOS.EM_DIAGNOSTICO);
        when(repositorio.buscarPorId(ordemDeServico.getId())).thenReturn(ordemDeServico);
        when(repositorio.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(eventPublisher).publicarOrcamentoSolicitado(any());
        doNothing().when(notificacaoEmail).enviarOrcamentoParaAprovacao(any(), any(), any());

        OrdemDeServicoDTO.Response response = service.enviarOrcamento(ordemDeServico.getId());

        assertThat(response.status()).isEqualTo("AGUARDANDO_APROVACAO");
        verify(eventPublisher).publicarOrcamentoSolicitado(any());
    }

    @Test
    @DisplayName("Deve aprovar OS — AGUARDANDO_APROVACAO → APROVADO")
    void deveAprovar() {
        ordemDeServico.transicionarPara(StatusOS.EM_DIAGNOSTICO);
        ordemDeServico.transicionarPara(StatusOS.AGUARDANDO_APROVACAO);
        when(repositorio.buscarPorNumero(ordemDeServico.getNumero())).thenReturn(ordemDeServico);
        when(repositorio.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(eventPublisher).publicarOsAprovada(any());
        doNothing().when(notificacaoEmail).enviarConfirmacaoAprovacao(any(), any());

        OrdemDeServicoDTO.StatusPublicoResponse response = service.aprovar(ordemDeServico.getNumero());

        assertThat(response.status()).isEqualTo("APROVADO");
        verify(eventPublisher).publicarOsAprovada(any());
    }

    @Test
    @DisplayName("Deve recusar OS — AGUARDANDO_APROVACAO → CANCELADA")
    void deveRecusar() {
        ordemDeServico.transicionarPara(StatusOS.EM_DIAGNOSTICO);
        ordemDeServico.transicionarPara(StatusOS.AGUARDANDO_APROVACAO);
        when(repositorio.buscarPorNumero(ordemDeServico.getNumero())).thenReturn(ordemDeServico);
        when(repositorio.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(eventPublisher).publicarOsCancelada(any());
        doNothing().when(notificacaoEmail).enviarConfirmacaoRecusa(any(), any());

        OrdemDeServicoDTO.StatusPublicoResponse response = service.recusar(ordemDeServico.getNumero());

        assertThat(response.status()).isEqualTo("CANCELADA");
    }

    @Test
    @DisplayName("Deve iniciar execução — APROVADO → EM_EXECUCAO")
    void deveIniciarExecucao() {
        ordemDeServico.transicionarPara(StatusOS.EM_DIAGNOSTICO);
        ordemDeServico.transicionarPara(StatusOS.AGUARDANDO_APROVACAO);
        ordemDeServico.transicionarPara(StatusOS.APROVADO);
        Atendente atendente = Atendente.builder().id(UUID.randomUUID()).nome("Carlos").build();
        OrdemDeServicoDTO.IniciarExecucaoRequest request = new OrdemDeServicoDTO.IniciarExecucaoRequest(atendente.getId());

        when(repositorio.buscarPorId(ordemDeServico.getId())).thenReturn(ordemDeServico);
        when(atendenteRepositorio.buscarPorId(atendente.getId())).thenReturn(atendente);
        when(repositorio.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemDeServicoDTO.Response response = service.iniciarExecucao(ordemDeServico.getId(), request);

        assertThat(response.status()).isEqualTo("EM_EXECUCAO");
    }

    @Test
    @DisplayName("Deve finalizar OS — EM_EXECUCAO → FINALIZADA")
    void deveFinalizar() {
        ordemDeServico.transicionarPara(StatusOS.EM_DIAGNOSTICO);
        ordemDeServico.transicionarPara(StatusOS.AGUARDANDO_APROVACAO);
        ordemDeServico.transicionarPara(StatusOS.APROVADO);
        ordemDeServico.transicionarPara(StatusOS.EM_EXECUCAO);
        when(repositorio.buscarPorId(ordemDeServico.getId())).thenReturn(ordemDeServico);
        when(repositorio.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemDeServicoDTO.Response response = service.finalizar(ordemDeServico.getId());

        assertThat(response.status()).isEqualTo("FINALIZADA");
    }

    @Test
    @DisplayName("Deve entregar OS — FINALIZADA → ENTREGUE")
    void deveEntregar() {
        ordemDeServico.transicionarPara(StatusOS.EM_DIAGNOSTICO);
        ordemDeServico.transicionarPara(StatusOS.AGUARDANDO_APROVACAO);
        ordemDeServico.transicionarPara(StatusOS.APROVADO);
        ordemDeServico.transicionarPara(StatusOS.EM_EXECUCAO);
        ordemDeServico.transicionarPara(StatusOS.FINALIZADA);
        when(repositorio.buscarPorId(ordemDeServico.getId())).thenReturn(ordemDeServico);
        when(repositorio.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemDeServicoDTO.Response response = service.entregar(ordemDeServico.getId());

        assertThat(response.status()).isEqualTo("ENTREGUE");
    }

    @Test
    @DisplayName("Deve adicionar peça à OS")
    void deveAdicionarPeca() {
        ItemPecaDTO.AdicionarRequest request = new ItemPecaDTO.AdicionarRequest(
                UUID.randomUUID(), "Filtro de óleo", 2, new BigDecimal("50.00"));
        when(repositorio.buscarPorId(ordemDeServico.getId())).thenReturn(ordemDeServico);
        when(repositorio.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemDeServicoDTO.Response response = service.adicionarPeca(ordemDeServico.getId(), request);

        assertThat(response.itensPeca()).hasSize(1);
    }

    @Test
    @DisplayName("Deve remover serviço da OS")
    void deveRemoverServico() {
        Servico servico = Servico.builder().id(UUID.randomUUID()).nome("Troca de óleo")
                .precoBase(new BigDecimal("150.00")).build();
        UUID itemId = UUID.randomUUID();
        ItemServico item = ItemServico.builder().id(itemId).servico(servico)
                .ordemDeServico(ordemDeServico).quantidade(1).precoUnitario(new BigDecimal("150.00")).build();
        ordemDeServico.getItensServico().add(item);

        when(repositorio.buscarPorId(ordemDeServico.getId())).thenReturn(ordemDeServico);
        when(repositorio.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemDeServicoDTO.Response response = service.removerServico(ordemDeServico.getId(), itemId);

        assertThat(response.itensServico()).isEmpty();
    }

    @Test
    @DisplayName("Deve remover peça da OS")
    void deveRemoverPeca() {
        UUID itemId = UUID.randomUUID();
        ItemPeca item = ItemPeca.builder().id(itemId).pecaId(UUID.randomUUID())
                .nomePeca("Filtro").ordemDeServico(ordemDeServico)
                .quantidade(1).precoUnitario(new BigDecimal("30.00")).build();
        ordemDeServico.getItensPeca().add(item);

        when(repositorio.buscarPorId(ordemDeServico.getId())).thenReturn(ordemDeServico);
        when(repositorio.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemDeServicoDTO.Response response = service.removerPeca(ordemDeServico.getId(), itemId);

        assertThat(response.itensPeca()).isEmpty();
    }
}
