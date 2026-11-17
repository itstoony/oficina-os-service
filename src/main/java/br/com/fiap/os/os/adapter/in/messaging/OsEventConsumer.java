package br.com.fiap.os.os.adapter.in.messaging;

import br.com.fiap.os.config.RabbitMQConfig;
import br.com.fiap.os.os.application.port.out.OrdemDeServicoRepositoryPort;
import br.com.fiap.os.os.domain.model.OrdemDeServico;
import br.com.fiap.os.os.domain.valueobject.StatusOS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OsEventConsumer {

    private final OrdemDeServicoRepositoryPort repositorio;

    @RabbitListener(queues = RabbitMQConfig.OS_QUEUE)
    public void consumir(Map<String, Object> evento,
                         @Header("amqp_receivedRoutingKey") String routingKey) {
        log.info("Evento recebido: {}", routingKey);
        try {
            switch (routingKey) {
                case RabbitMQConfig.ROUTING_PAGAMENTO_CONFIRMADO -> processarPagamentoConfirmado(evento);
                case RabbitMQConfig.ROUTING_PAGAMENTO_RECUSADO -> processarPagamentoRecusado(evento);
                case RabbitMQConfig.ROUTING_ESTOQUE_INDISPONIVEL -> processarEstoqueIndisponivel(evento);
                case RabbitMQConfig.ROUTING_EXECUCAO_FINALIZADA -> processarExecucaoFinalizada(evento);
                default -> log.debug("Evento ignorado pelo os-service: {}", routingKey);
            }
        } catch (Exception e) {
            log.error("Erro ao processar evento {}: {}", routingKey, e.getMessage(), e);
        }
    }

    @Transactional
    protected void processarExecucaoFinalizada(Map<String, Object> evento) {
        String osId = (String) evento.get("osId");
        if (osId == null) return;

        OrdemDeServico ordemDeServico = repositorio.buscarPorId(UUID.fromString(osId));
        if (ordemDeServico.getStatus() == StatusOS.EM_EXECUCAO) {
            ordemDeServico.transicionarPara(StatusOS.FINALIZADA);
            repositorio.salvar(ordemDeServico);
            log.info("OS {} finalizada via evento execucao.finalizada", ordemDeServico.getNumero());
        }
    }

    @Transactional
    protected void processarPagamentoConfirmado(Map<String, Object> evento) {
        String osId = (String) evento.get("osId");
        if (osId == null) return;

        OrdemDeServico ordemDeServico = repositorio.buscarPorId(UUID.fromString(osId));
        if (ordemDeServico.getStatus() == StatusOS.APROVADO) {
            ordemDeServico.transicionarPara(StatusOS.EM_EXECUCAO);
            repositorio.salvar(ordemDeServico);
            log.info("OS {} em execução via evento pagamento.confirmado", ordemDeServico.getNumero());
        }
    }

    @Transactional
    protected void processarEstoqueIndisponivel(Map<String, Object> evento) {
        String osId = (String) evento.get("osId");
        if (osId == null) return;

        OrdemDeServico ordemDeServico = repositorio.buscarPorId(UUID.fromString(osId));
        StatusOS status = ordemDeServico.getStatus();
        if (status == StatusOS.APROVADO || status == StatusOS.AGUARDANDO_APROVACAO) {
            ordemDeServico.transicionarPara(StatusOS.CANCELADA);
            repositorio.salvar(ordemDeServico);
            log.info("OS {} cancelada via evento estoque.indisponivel (estava: {})", ordemDeServico.getNumero(), status);
        }
    }

    @Transactional
    protected void processarPagamentoRecusado(Map<String, Object> evento) {
        String osId = (String) evento.get("osId");
        if (osId == null) return;

        OrdemDeServico ordemDeServico = repositorio.buscarPorId(UUID.fromString(osId));
        if (ordemDeServico.getStatus() == StatusOS.APROVADO) {
            ordemDeServico.transicionarPara(StatusOS.CANCELADA);
            repositorio.salvar(ordemDeServico);
            log.info("OS {} cancelada por pagamento recusado", ordemDeServico.getNumero());
        }
    }
}
