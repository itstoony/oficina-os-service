package br.com.fiap.os.os.adapter.out.messaging;

import br.com.fiap.os.config.RabbitMQConfig;
import br.com.fiap.os.os.application.port.out.OsEventPublisherPort;
import br.com.fiap.os.os.domain.model.ItemPeca;
import br.com.fiap.os.os.domain.model.OrdemDeServico;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OsEventPublisher implements OsEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publicarOsAberta(OrdemDeServico ordemDeServico) {
        Map<String, Object> evento = Map.of(
                "osId", ordemDeServico.getId().toString(),
                "numero", ordemDeServico.getNumero(),
                "clienteId", ordemDeServico.getCliente().getId().toString(),
                "nomeCliente", ordemDeServico.getCliente().getNome(),
                "veiculoId", ordemDeServico.getVeiculo().getId().toString()
        );
        publicar("os.aberta", evento);
    }

    @Override
    public void publicarOsAprovada(OrdemDeServico ordemDeServico) {
        List<Map<String, Object>> pecas = ordemDeServico.getItensPeca().stream()
                .map(this::toPecaMap)
                .collect(Collectors.toList());

        Map<String, Object> evento = Map.of(
                "osId", ordemDeServico.getId().toString(),
                "numero", ordemDeServico.getNumero(),
                "clienteId", ordemDeServico.getCliente().getId().toString(),
                "clienteEmail", ordemDeServico.getCliente().getEmail(),
                "valorTotal", ordemDeServico.getValorTotal(),
                "pecas", pecas
        );
        publicar("os.aprovada", evento);
    }

    @Override
    public void publicarOsCancelada(OrdemDeServico ordemDeServico) {
        Map<String, Object> evento = Map.of(
                "osId", ordemDeServico.getId().toString(),
                "numero", ordemDeServico.getNumero()
        );
        publicar("os.cancelada", evento);
    }

    @Override
    public void publicarOrcamentoSolicitado(OrdemDeServico ordemDeServico) {
        List<Map<String, Object>> servicos = ordemDeServico.getItensServico().stream()
                .map(item -> Map.<String, Object>of(
                        "servicoId", item.getServico().getId().toString(),
                        "nome", item.getServico().getNome(),
                        "quantidade", item.getQuantidade(),
                        "precoUnitario", item.getPrecoUnitario()
                ))
                .collect(Collectors.toList());

        List<Map<String, Object>> pecas = ordemDeServico.getItensPeca().stream()
                .map(this::toPecaMap)
                .collect(Collectors.toList());

        Map<String, Object> evento = Map.of(
                "osId", ordemDeServico.getId().toString(),
                "numero", ordemDeServico.getNumero(),
                "clienteId", ordemDeServico.getCliente().getId().toString(),
                "clienteEmail", ordemDeServico.getCliente().getEmail(),
                "valorTotal", ordemDeServico.getValorTotal(),
                "servicos", servicos,
                "pecas", pecas
        );
        publicar("orcamento.solicitado", evento);
    }

    private Map<String, Object> toPecaMap(ItemPeca item) {
        return Map.of(
                "pecaId", item.getPecaId().toString(),
                "nome", item.getNomePeca(),
                "quantidade", item.getQuantidade(),
                "precoUnitario", item.getPrecoUnitario()
        );
    }

    private void publicar(String routingKey, Object payload) {
        log.info("Publicando evento: {}", routingKey);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, payload);
    }
}
