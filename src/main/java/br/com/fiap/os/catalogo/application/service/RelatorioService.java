package br.com.fiap.os.catalogo.application.service;

import br.com.fiap.os.catalogo.application.port.in.RelatorioDTO;
import br.com.fiap.os.catalogo.application.port.in.RelatorioUseCase;
import br.com.fiap.os.os.application.port.out.OrdemDeServicoRepositoryPort;
import br.com.fiap.os.os.domain.model.OrdemDeServico;
import br.com.fiap.os.os.domain.valueobject.StatusOS;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioService implements RelatorioUseCase {

    private final OrdemDeServicoRepositoryPort repositorio;

    @Override
    @Transactional(readOnly = true)
    public RelatorioDTO.TempoMedioResponse calcularTempoMedioExecucao() {
        List<OrdemDeServico> ordens = repositorio.buscarFinalizadasComDatas(
                List.of(StatusOS.FINALIZADA, StatusOS.ENTREGUE));

        if (ordens.isEmpty()) {
            return new RelatorioDTO.TempoMedioResponse(0, 0, "Sem dados");
        }

        long totalMinutos = ordens.stream()
                .mapToLong(os -> ChronoUnit.MINUTES.between(os.getDataInicioExecucao(), os.getDataFimExecucao()))
                .sum();

        long mediaMinutos = totalMinutos / ordens.size();
        return new RelatorioDTO.TempoMedioResponse(ordens.size(), mediaMinutos, formatarDuracao(mediaMinutos));
    }

    private String formatarDuracao(long minutos) {
        long horas = minutos / 60;
        long mins = minutos % 60;
        return horas > 0 ? horas + "h " + mins + "m" : mins + "m";
    }
}
