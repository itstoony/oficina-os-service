package br.com.fiap.os.os.application.port.out;

import br.com.fiap.os.os.domain.model.OrdemDeServico;

public interface OsEventPublisherPort {
    void publicarOsAberta(OrdemDeServico ordemDeServico);
    void publicarOsAprovada(OrdemDeServico ordemDeServico);
    void publicarOsCancelada(OrdemDeServico ordemDeServico);
    void publicarOrcamentoSolicitado(OrdemDeServico ordemDeServico);
}
