package br.com.fiap.os.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "oficina.events";
    public static final String OS_QUEUE = "os.eventos.queue";

    public static final String ROUTING_ORCAMENTO_GERADO    = "orcamento.gerado";
    public static final String ROUTING_PAGAMENTO_CONFIRMADO = "pagamento.confirmado";
    public static final String ROUTING_PAGAMENTO_RECUSADO  = "pagamento.recusado";
    public static final String ROUTING_ESTOQUE_RESERVADO   = "estoque.reservado";
    public static final String ROUTING_ESTOQUE_INDISPONIVEL = "estoque.indisponivel";
    public static final String ROUTING_EXECUCAO_FINALIZADA = "execucao.finalizada";

    @Bean
    public TopicExchange oficinaTopic() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue osQueue() {
        return QueueBuilder.durable(OS_QUEUE).build();
    }

    @Bean
    public Binding bindingOrcamentoGerado(Queue osQueue, TopicExchange oficinaTopic) {
        return BindingBuilder.bind(osQueue).to(oficinaTopic).with(ROUTING_ORCAMENTO_GERADO);
    }

    @Bean
    public Binding bindingPagamentoConfirmado(Queue osQueue, TopicExchange oficinaTopic) {
        return BindingBuilder.bind(osQueue).to(oficinaTopic).with(ROUTING_PAGAMENTO_CONFIRMADO);
    }

    @Bean
    public Binding bindingPagamentoRecusado(Queue osQueue, TopicExchange oficinaTopic) {
        return BindingBuilder.bind(osQueue).to(oficinaTopic).with(ROUTING_PAGAMENTO_RECUSADO);
    }

    @Bean
    public Binding bindingEstoqueReservado(Queue osQueue, TopicExchange oficinaTopic) {
        return BindingBuilder.bind(osQueue).to(oficinaTopic).with(ROUTING_ESTOQUE_RESERVADO);
    }

    @Bean
    public Binding bindingEstoqueIndisponivel(Queue osQueue, TopicExchange oficinaTopic) {
        return BindingBuilder.bind(osQueue).to(oficinaTopic).with(ROUTING_ESTOQUE_INDISPONIVEL);
    }

    @Bean
    public Binding bindingExecucaoFinalizada(Queue osQueue, TopicExchange oficinaTopic) {
        return BindingBuilder.bind(osQueue).to(oficinaTopic).with(ROUTING_EXECUCAO_FINALIZADA);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
