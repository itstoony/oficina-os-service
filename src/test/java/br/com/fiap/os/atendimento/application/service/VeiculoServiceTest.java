package br.com.fiap.os.atendimento.application.service;

import br.com.fiap.os.atendimento.application.port.in.VeiculoDTO;
import br.com.fiap.os.atendimento.application.port.out.ClienteRepositoryPort;
import br.com.fiap.os.atendimento.application.port.out.VeiculoRepositoryPort;
import br.com.fiap.os.atendimento.domain.model.Cliente;
import br.com.fiap.os.atendimento.domain.model.Veiculo;
import br.com.fiap.os.atendimento.domain.valueobject.Placa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock private VeiculoRepositoryPort repositorio;
    @Mock private ClienteRepositoryPort clienteRepositorio;

    @InjectMocks private VeiculoService service;

    private Cliente cliente;
    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder().id(UUID.randomUUID()).nome("João").email("j@j.com").build();
        veiculo = Veiculo.builder()
                .id(UUID.randomUUID()).marca("Toyota").modelo("Corolla")
                .ano(2022).cor("Prata").placa(Placa.of("ABC1234")).cliente(cliente).build();
    }

    @Test
    @DisplayName("Deve cadastrar veículo")
    void deveCadastrarVeiculo() {
        VeiculoDTO.CadastrarRequest request = new VeiculoDTO.CadastrarRequest(
                "Toyota", "Corolla", 2022, "Prata", "ABC1234", cliente.getId());
        when(clienteRepositorio.buscarPorId(cliente.getId())).thenReturn(cliente);
        when(repositorio.salvar(any())).thenReturn(veiculo);

        VeiculoDTO.Response response = service.cadastrar(request);

        assertThat(response.marca()).isEqualTo("Toyota");
        verify(repositorio).salvar(any());
    }

    @Test
    @DisplayName("Deve atualizar veículo")
    void deveAtualizarVeiculo() {
        VeiculoDTO.AtualizarRequest request = new VeiculoDTO.AtualizarRequest("Honda", "Civic", 2023, "Preto");
        when(repositorio.buscarPorId(veiculo.getId())).thenReturn(veiculo);
        when(repositorio.salvar(any())).thenReturn(veiculo);

        VeiculoDTO.Response response = service.atualizar(veiculo.getId(), request);

        assertThat(response).isNotNull();
        verify(repositorio).salvar(veiculo);
    }

    @Test
    @DisplayName("Deve buscar veículo por id")
    void deveBuscarPorId() {
        when(repositorio.buscarPorId(veiculo.getId())).thenReturn(veiculo);

        VeiculoDTO.Response response = service.buscarPorId(veiculo.getId());

        assertThat(response.modelo()).isEqualTo("Corolla");
    }

    @Test
    @DisplayName("Deve listar veículos")
    void deveListar() {
        when(repositorio.listar()).thenReturn(List.of(veiculo));

        List<VeiculoDTO.Response> lista = service.listar();

        assertThat(lista).hasSize(1);
    }

    @Test
    @DisplayName("Deve listar veículos por cliente")
    void deveListarPorCliente() {
        when(repositorio.listarPorClienteId(cliente.getId())).thenReturn(List.of(veiculo));

        List<VeiculoDTO.Response> lista = service.listarPorCliente(cliente.getId());

        assertThat(lista).hasSize(1);
    }

    @Test
    @DisplayName("Deve deletar veículo")
    void deveDeletar() {
        when(repositorio.buscarPorId(veiculo.getId())).thenReturn(veiculo);
        doNothing().when(repositorio).deletar(veiculo.getId());

        service.deletar(veiculo.getId());

        verify(repositorio).deletar(veiculo.getId());
    }
}
