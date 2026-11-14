package br.com.fiap.os.atendimento.application.service;

import br.com.fiap.os.atendimento.application.port.in.AtendenteDTO;
import br.com.fiap.os.atendimento.application.port.out.AtendenteRepositoryPort;
import br.com.fiap.os.atendimento.domain.model.Atendente;
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
class AtendenteServiceTest {

    @Mock private AtendenteRepositoryPort repositorio;

    @InjectMocks private AtendenteService service;

    private Atendente atendente;

    @BeforeEach
    void setUp() {
        atendente = Atendente.builder()
                .id(UUID.randomUUID()).nome("Carlos").email("carlos@oficina.com").telefone("11999999999").build();
    }

    @Test
    @DisplayName("Deve cadastrar atendente")
    void deveCadastrar() {
        AtendenteDTO.CadastrarRequest request = new AtendenteDTO.CadastrarRequest("Carlos", "carlos@oficina.com", "11999999999");
        when(repositorio.salvar(any())).thenReturn(atendente);

        AtendenteDTO.Response response = service.cadastrar(request);

        assertThat(response.nome()).isEqualTo("Carlos");
        verify(repositorio).salvar(any());
    }

    @Test
    @DisplayName("Deve atualizar atendente")
    void deveAtualizar() {
        AtendenteDTO.AtualizarRequest request = new AtendenteDTO.AtualizarRequest("Carlos B.", "carlosb@oficina.com", "11988888888");
        when(repositorio.buscarPorId(atendente.getId())).thenReturn(atendente);
        when(repositorio.salvar(any())).thenReturn(atendente);

        AtendenteDTO.Response response = service.atualizar(atendente.getId(), request);

        assertThat(response).isNotNull();
        verify(repositorio).salvar(atendente);
    }

    @Test
    @DisplayName("Deve buscar atendente por id")
    void deveBuscarPorId() {
        when(repositorio.buscarPorId(atendente.getId())).thenReturn(atendente);

        AtendenteDTO.Response response = service.buscarPorId(atendente.getId());

        assertThat(response.email()).isEqualTo("carlos@oficina.com");
    }

    @Test
    @DisplayName("Deve listar atendentes")
    void deveListar() {
        when(repositorio.listar()).thenReturn(List.of(atendente));

        List<AtendenteDTO.Response> lista = service.listar();

        assertThat(lista).hasSize(1);
    }

    @Test
    @DisplayName("Deve deletar atendente")
    void deveDeletar() {
        when(repositorio.buscarPorId(atendente.getId())).thenReturn(atendente);
        doNothing().when(repositorio).deletar(atendente.getId());

        service.deletar(atendente.getId());

        verify(repositorio).deletar(atendente.getId());
    }
}
