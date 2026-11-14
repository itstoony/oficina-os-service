package br.com.fiap.os.os.application.port.out;

import br.com.fiap.os.os.domain.model.OrdemDeServico;
import br.com.fiap.os.os.domain.valueobject.StatusOS;

import java.util.List;
import java.util.UUID;

public interface OrdemDeServicoRepositoryPort {
    OrdemDeServico salvar(OrdemDeServico ordemDeServico);
    OrdemDeServico buscarPorId(UUID id);
    OrdemDeServico buscarPorNumero(String numero);
    List<OrdemDeServico> listarAtivasOrdenadas();
    int buscarProximoSequencial(String prefixo);
    List<OrdemDeServico> buscarFinalizadasComDatas(List<StatusOS> statuses);
}
