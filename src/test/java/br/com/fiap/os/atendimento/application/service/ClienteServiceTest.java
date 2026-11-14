package br.com.fiap.os.atendimento.application.service;

import br.com.fiap.os.atendimento.application.port.in.ClienteDTO;
import br.com.fiap.os.atendimento.application.port.out.ClienteRepositoryPort;
import br.com.fiap.os.atendimento.domain.model.Cliente;
import br.com.fiap.os.atendimento.domain.valueobject.Documento;
import br.com.fiap.os.shared.exception.RecursoNaoEncontradoException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepositoryPort repositorio;

    @InjectMocks
    private ClienteService service;

    private Cliente clienteExistente;

    @BeforeEach
    void setUp() {
        clienteExistente = Cliente.builder()
                .id(UUID.randomUUID())
                .nome("Maria Souza")
                .email("maria@email.com")
                .telefone("11888888888")
                .documento(Documento.of("529.982.247-25"))
                .build();
    }

    @Test
    @DisplayName("Deve cadastrar cliente com documento válido")
    void deveCadastrarCliente() {
        when(repositorio.salvar(any())).thenReturn(clienteExistente);

        ClienteDTO.Response response = service.cadastrar(new ClienteDTO.CadastrarRequest(
                "Maria Souza", "maria@email.com", "11888888888", "529.982.247-25"
        ));

        assertThat(response.nome()).isEqualTo("Maria Souza");
        assertThat(response.tipoDocumento()).isEqualTo("CPF");
        verify(repositorio).salvar(any());
    }

    @Test
    @DisplayName("Deve listar todos os clientes")
    void deveListarClientes() {
        when(repositorio.listar()).thenReturn(List.of(clienteExistente));

        List<ClienteDTO.Response> lista = service.listar();

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).nome()).isEqualTo("Maria Souza");
    }

    @Test
    @DisplayName("Deve atualizar dados do cliente")
    void deveAtualizarCliente() {
        when(repositorio.buscarPorId(clienteExistente.getId())).thenReturn(clienteExistente);
        when(repositorio.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        ClienteDTO.Response response = service.atualizar(clienteExistente.getId(),
                new ClienteDTO.AtualizarRequest("Maria Atualizada", "novo@email.com", "11777777777"));

        assertThat(response.nome()).isEqualTo("Maria Atualizada");
        assertThat(response.email()).isEqualTo("novo@email.com");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar cliente inexistente")
    void deveLancarExcecaoClienteInexistente() {
        UUID idInexistente = UUID.randomUUID();
        when(repositorio.buscarPorId(idInexistente))
                .thenThrow(new RecursoNaoEncontradoException("Cliente não encontrado: " + idInexistente));

        assertThatThrownBy(() -> service.buscarPorId(idInexistente))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve deletar cliente existente")
    void deveDeletarCliente() {
        when(repositorio.buscarPorId(clienteExistente.getId())).thenReturn(clienteExistente);
        doNothing().when(repositorio).deletar(clienteExistente.getId());

        service.deletar(clienteExistente.getId());

        verify(repositorio).deletar(clienteExistente.getId());
    }
}
