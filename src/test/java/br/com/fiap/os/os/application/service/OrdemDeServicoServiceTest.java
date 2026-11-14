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
import br.com.fiap.os.os.application.port.in.ItemServicoDTO;
import br.com.fiap.os.os.application.port.in.OrdemDeServicoDTO;
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
}
