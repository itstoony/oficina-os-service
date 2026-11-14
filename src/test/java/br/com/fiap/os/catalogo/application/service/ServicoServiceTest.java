package br.com.fiap.os.catalogo.application.service;

import br.com.fiap.os.catalogo.application.port.in.ServicoDTO;
import br.com.fiap.os.catalogo.application.port.out.ServicoRepositoryPort;
import br.com.fiap.os.catalogo.domain.model.Servico;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock private ServicoRepositoryPort repositorio;

    @InjectMocks private ServicoService service;

    private Servico servico;

    @BeforeEach
    void setUp() {
        servico = Servico.builder()
                .id(UUID.randomUUID()).nome("Troca de óleo")
                .descricao("Troca de óleo e filtro").precoBase(new BigDecimal("150.00")).build();
    }

    @Test
    @DisplayName("Deve criar serviço")
    void deveCriar() {
        ServicoDTO.CriarRequest request = new ServicoDTO.CriarRequest("Troca de óleo", "Troca de óleo e filtro", new BigDecimal("150.00"));
        when(repositorio.salvar(any())).thenReturn(servico);

        ServicoDTO.Response response = service.criar(request);

        assertThat(response.nome()).isEqualTo("Troca de óleo");
        verify(repositorio).salvar(any());
    }

    @Test
    @DisplayName("Deve atualizar serviço")
    void deveAtualizar() {
        ServicoDTO.AtualizarRequest request = new ServicoDTO.AtualizarRequest("Alinhamento", "Alinhamento de rodas", new BigDecimal("200.00"));
        when(repositorio.buscarPorId(servico.getId())).thenReturn(servico);
        when(repositorio.salvar(any())).thenReturn(servico);

        ServicoDTO.Response response = service.atualizar(servico.getId(), request);

        assertThat(response).isNotNull();
        verify(repositorio).salvar(servico);
    }

    @Test
    @DisplayName("Deve buscar serviço por id")
    void deveBuscarPorId() {
        when(repositorio.buscarPorId(servico.getId())).thenReturn(servico);

        ServicoDTO.Response response = service.buscarPorId(servico.getId());

        assertThat(response.precoBase()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("Deve listar serviços")
    void deveListar() {
        when(repositorio.listar()).thenReturn(List.of(servico));

        List<ServicoDTO.Response> lista = service.listar();

        assertThat(lista).hasSize(1);
    }

    @Test
    @DisplayName("Deve deletar serviço")
    void deveDeletar() {
        when(repositorio.buscarPorId(servico.getId())).thenReturn(servico);
        doNothing().when(repositorio).deletar(servico.getId());

        service.deletar(servico.getId());

        verify(repositorio).deletar(servico.getId());
    }
}
