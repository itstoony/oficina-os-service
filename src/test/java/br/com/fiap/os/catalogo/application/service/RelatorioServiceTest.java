package br.com.fiap.os.catalogo.application.service;

import br.com.fiap.os.catalogo.application.port.in.RelatorioDTO;
import br.com.fiap.os.os.application.port.out.OrdemDeServicoRepositoryPort;
import br.com.fiap.os.os.domain.model.OrdemDeServico;
import br.com.fiap.os.os.domain.valueobject.StatusOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock private OrdemDeServicoRepositoryPort repositorio;

    @InjectMocks
    private RelatorioService service;

    @Test
    @DisplayName("Deve retornar 'Sem dados' quando não há OSs finalizadas")
    void deveRetornarSemDadosQuandoVazio() {
        when(repositorio.buscarFinalizadasComDatas(anyList())).thenReturn(List.of());

        RelatorioDTO.TempoMedioResponse response = service.calcularTempoMedioExecucao();

        assertThat(response.totalOrdens()).isZero();
        assertThat(response.mediaFormatada()).isEqualTo("Sem dados");
    }

    @Test
    @DisplayName("Deve calcular tempo médio em minutos")
    void deveCalcularTempoMedioEmMinutos() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 14, 8, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 11, 14, 8, 45);

        OrdemDeServico os = OrdemDeServico.builder()
                .id(UUID.randomUUID())
                .numero("OS-2026-00001")
                .status(StatusOS.FINALIZADA)
                .dataInicioExecucao(inicio)
                .dataFimExecucao(fim)
                .build();

        when(repositorio.buscarFinalizadasComDatas(anyList())).thenReturn(List.of(os));

        RelatorioDTO.TempoMedioResponse response = service.calcularTempoMedioExecucao();

        assertThat(response.totalOrdens()).isEqualTo(1);
        assertThat(response.mediaMinutos()).isEqualTo(45);
        assertThat(response.mediaFormatada()).isEqualTo("45m");
    }

    @Test
    @DisplayName("Deve formatar duração em horas e minutos")
    void deveFormatarDuracaoEmHorasEMinutos() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 14, 8, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 11, 14, 10, 30);

        OrdemDeServico os = OrdemDeServico.builder()
                .id(UUID.randomUUID())
                .numero("OS-2026-00001")
                .status(StatusOS.ENTREGUE)
                .dataInicioExecucao(inicio)
                .dataFimExecucao(fim)
                .build();

        when(repositorio.buscarFinalizadasComDatas(anyList())).thenReturn(List.of(os));

        RelatorioDTO.TempoMedioResponse response = service.calcularTempoMedioExecucao();

        assertThat(response.mediaMinutos()).isEqualTo(150);
        assertThat(response.mediaFormatada()).isEqualTo("2h 30m");
    }

    @Test
    @DisplayName("Deve calcular média entre múltiplas OSs")
    void deveCalcularMediaEntreMultiplasOs() {
        LocalDateTime base = LocalDateTime.of(2026, 11, 14, 8, 0);

        OrdemDeServico os1 = OrdemDeServico.builder()
                .id(UUID.randomUUID()).numero("OS-2026-00001").status(StatusOS.FINALIZADA)
                .dataInicioExecucao(base).dataFimExecucao(base.plusMinutes(60)).build();

        OrdemDeServico os2 = OrdemDeServico.builder()
                .id(UUID.randomUUID()).numero("OS-2026-00002").status(StatusOS.ENTREGUE)
                .dataInicioExecucao(base).dataFimExecucao(base.plusMinutes(120)).build();

        when(repositorio.buscarFinalizadasComDatas(anyList())).thenReturn(List.of(os1, os2));

        RelatorioDTO.TempoMedioResponse response = service.calcularTempoMedioExecucao();

        assertThat(response.totalOrdens()).isEqualTo(2);
        assertThat(response.mediaMinutos()).isEqualTo(90);
        assertThat(response.mediaFormatada()).isEqualTo("1h 30m");
    }
}
