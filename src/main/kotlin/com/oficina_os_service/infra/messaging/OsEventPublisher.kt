package com.oficina_os_service.infra.messaging

import com.oficina_os_service.infra.messaging.config.RabbitMQConfig
import com.oficina_os_service.infra.messaging.events.*
import com.oficina_os_service.infra.repository.OrdemServicoEntity
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class OsEventPublisher(private val rabbitTemplate: RabbitTemplate) {
  
  private val log = LoggerFactory.getLogger(OsEventPublisher::class.java)
  
  fun publicarOsCriada(os: OrdemServicoEntity) {
    val event = OsCriadaEvent(
      osId = os.id!!,
      clienteId = os.clienteId,
      veiculoId = os.veiculoId,
      itensServico = os.itensServico.map {
        ItemServicoEventDto(it.servicoId, it.quantidade, it.precoAplicado)
      },
      itensEstoque = os.itensEstoque.map {
        ItemEstoqueEventDto(it.estoqueId, it.quantidade, it.precoUnitario)
      }
    )
    rabbitTemplate.convertAndSend(RabbitMQConfig.OS_EXCHANGE, RabbitMQConfig.ROUTING_OS_CRIADA, event)
    log.info("Evento os.criada publicado para OS id={}", os.id)
  }
  
  fun publicarOsCancelada(osId: Long, motivo: String?) {
    val event = OsCanceladaEvent(osId = osId, motivo = motivo)
    rabbitTemplate.convertAndSend(RabbitMQConfig.OS_EXCHANGE, RabbitMQConfig.ROUTING_OS_CANCELADA, event)
    log.info("Evento os.cancelada publicado para OS id={}", osId)
  }
}
