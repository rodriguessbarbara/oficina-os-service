package com.oficina_os_service.infra.messaging.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {
  
  companion object {
    const val OS_EXCHANGE = "os.exchange"
    
    const val ROUTING_OS_CRIADA       = "os.criada"
    const val ROUTING_OS_CANCELADA    = "os.cancelada"
    
    // Filas de entrada (OS Service consome)
    const val QUEUE_ORCAMENTO_GERADO   = "orcamento.gerado.os-service"
    const val QUEUE_ORCAMENTO_APROVADO  = "orcamento.aprovado.os-service"
    const val QUEUE_ORCAMENTO_REJEITADO = "orcamento.rejeitado.os-service"
    const val QUEUE_PAGAMENTO_CONFIRMADO = "pagamento.confirmado.os-service"
    const val QUEUE_PAGAMENTO_FALHOU    = "pagamento.falhou.os-service"
    const val QUEUE_EXECUCAO_INICIADA   = "execucao.iniciada.os-service"
    const val QUEUE_EXECUCAO_FINALIZADA = "execucao.finalizada.os-service"
    
    // Routing keys para eventos que chegam ao OS Service
    const val ROUTING_ORCAMENTO_GERADO   = "orcamento.gerado"
    const val ROUTING_ORCAMENTO_APROVADO  = "orcamento.aprovado"
    const val ROUTING_ORCAMENTO_REJEITADO = "orcamento.rejeitado"
    const val ROUTING_PAGAMENTO_CONFIRMADO = "pagamento.confirmado"
    const val ROUTING_PAGAMENTO_FALHOU    = "pagamento.falhou"
    const val ROUTING_EXECUCAO_INICIADA   = "execucao.iniciada"
    const val ROUTING_EXECUCAO_FINALIZADA = "execucao.finalizada"
  }
  
  @Bean fun osExchange(): TopicExchange = TopicExchange(OS_EXCHANGE, true, false)
  
  // Filas que o OS Service consome
  @Bean fun queueOrcamentoGerado()    = Queue(QUEUE_ORCAMENTO_GERADO, true)
  @Bean fun queueOrcamentoAprovado()  = Queue(QUEUE_ORCAMENTO_APROVADO, true)
  @Bean fun queueOrcamentoRejeitado() = Queue(QUEUE_ORCAMENTO_REJEITADO, true)
  @Bean fun queuePagamentoConfirmado() = Queue(QUEUE_PAGAMENTO_CONFIRMADO, true)
  @Bean fun queuePagamentoFalhou()    = Queue(QUEUE_PAGAMENTO_FALHOU, true)
  @Bean fun queueExecucaoIniciada()   = Queue(QUEUE_EXECUCAO_INICIADA, true)
  @Bean fun queueExecucaoFinalizada() = Queue(QUEUE_EXECUCAO_FINALIZADA, true)
  
  @Bean fun bindingOrcamentoGerado(osExchange: TopicExchange)    =
    BindingBuilder.bind(queueOrcamentoGerado()).to(osExchange).with(ROUTING_ORCAMENTO_GERADO)
  @Bean fun bindingOrcamentoAprovado(osExchange: TopicExchange)  =
    BindingBuilder.bind(queueOrcamentoAprovado()).to(osExchange).with(ROUTING_ORCAMENTO_APROVADO)
  @Bean fun bindingOrcamentoRejeitado(osExchange: TopicExchange) =
    BindingBuilder.bind(queueOrcamentoRejeitado()).to(osExchange).with(ROUTING_ORCAMENTO_REJEITADO)
  @Bean fun bindingPagamentoConfirmado(osExchange: TopicExchange) =
    BindingBuilder.bind(queuePagamentoConfirmado()).to(osExchange).with(ROUTING_PAGAMENTO_CONFIRMADO)
  @Bean fun bindingPagamentoFalhou(osExchange: TopicExchange)    =
    BindingBuilder.bind(queuePagamentoFalhou()).to(osExchange).with(ROUTING_PAGAMENTO_FALHOU)
  @Bean fun bindingExecucaoIniciada(osExchange: TopicExchange)   =
    BindingBuilder.bind(queueExecucaoIniciada()).to(osExchange).with(ROUTING_EXECUCAO_INICIADA)
  @Bean fun bindingExecucaoFinalizada(osExchange: TopicExchange) =
    BindingBuilder.bind(queueExecucaoFinalizada()).to(osExchange).with(ROUTING_EXECUCAO_FINALIZADA)
  
  @Bean fun messageConverter(mapper: ObjectMapper) = Jackson2JsonMessageConverter(mapper)
  
  @Bean fun rabbitTemplate(connectionFactory: ConnectionFactory, converter: Jackson2JsonMessageConverter): RabbitTemplate =
    RabbitTemplate(connectionFactory).also { it.messageConverter = converter }
}
