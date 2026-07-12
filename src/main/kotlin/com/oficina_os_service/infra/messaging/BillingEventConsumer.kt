package com.oficina_os_service.infra.messaging

import com.oficina_os_service.application.OrdemServicoService
import com.oficina_os_service.domain.enum.StatusOS
import com.oficina_os_service.infra.messaging.config.RabbitMQConfig
import com.oficina_os_service.infra.messaging.events.*
import com.oficina_os_service.infra.nosql.OsResumoDocument
import com.oficina_os_service.infra.nosql.OsResumoRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class BillingEventConsumer(
  private val osService: OrdemServicoService,
  private val resumoRepository: OsResumoRepository
) {
  
  private val log = LoggerFactory.getLogger(BillingEventConsumer::class.java)
  
  @RabbitListener(queues = [RabbitMQConfig.QUEUE_ORCAMENTO_GERADO])
  fun onOrcamentoGerado(event: OrcamentoGeradoEvent) {
    log.info("Recebido orcamento.gerado osId={} orcamentoId={}", event.osId, event.orcamentoId)
    val resumo = resumoRepository.findById(event.osId).orElse(OsResumoDocument(osId = event.osId))
    resumoRepository.save(
      resumo.copy(
        orcamentoId = event.orcamentoId,
        valorOrcamento = event.valorTotal,
        statusOrcamento = "PENDENTE",
        ultimaAtualizacao = Instant.now()
      )
    )
    osService.atualizarStatus(event.osId, StatusOS.AGUARDANDO_APROVACAO, "Orçamento gerado pelo Billing Service")
  }
  
  @RabbitListener(queues = [RabbitMQConfig.QUEUE_ORCAMENTO_APROVADO])
  fun onOrcamentoAprovado(event: OrcamentoAprovadoEvent) {
    log.info("Recebido orcamento.aprovado osId={}", event.osId)
    resumoRepository.findById(event.osId).ifPresent { resumo ->
      resumoRepository.save(resumo.copy(statusOrcamento = "APROVADO", ultimaAtualizacao = Instant.now()))
    }
    osService.atualizarStatus(event.osId, StatusOS.APROVADA, "Orçamento aprovado pelo cliente")
  }
  
  @RabbitListener(queues = [RabbitMQConfig.QUEUE_ORCAMENTO_REJEITADO])
  fun onOrcamentoRejeitado(event: OrcamentoRejeitadoEvent) {
    log.info("Recebido orcamento.rejeitado osId={}", event.osId)
    resumoRepository.findById(event.osId).ifPresent { resumo ->
      resumoRepository.save(resumo.copy(statusOrcamento = "REJEITADO", ultimaAtualizacao = Instant.now()))
    }
    osService.cancelar(event.osId, event.motivo ?: "Orçamento rejeitado pelo cliente")
  }
  
  @RabbitListener(queues = [RabbitMQConfig.QUEUE_PAGAMENTO_CONFIRMADO])
  fun onPagamentoConfirmado(event: PagamentoConfirmadoEvent) {
    log.info("Recebido pagamento.confirmado osId={} pagamentoId={}", event.osId, event.pagamentoId)
    resumoRepository.findById(event.osId).ifPresent { resumo ->
      resumoRepository.save(
        resumo.copy(
          pagamentoId = event.pagamentoId,
          statusPagamento = "CONFIRMADO",
          ultimaAtualizacao = Instant.now()
        )
      )
    }
    osService.atualizarStatus(event.osId, StatusOS.APROVADA, "Pagamento confirmado")
  }
  
  @RabbitListener(queues = [RabbitMQConfig.QUEUE_PAGAMENTO_FALHOU])
  fun onPagamentoFalhou(event: PagamentoFalhouEvent) {
    log.info("Recebido pagamento.falhou osId={}", event.osId)
    resumoRepository.findById(event.osId).ifPresent { resumo ->
      resumoRepository.save(resumo.copy(statusPagamento = "FALHOU", ultimaAtualizacao = Instant.now()))
    }
    osService.cancelar(event.osId, event.motivo ?: "Pagamento falhou")
  }
}
