package com.oficina_os_service.infra.messaging

import com.oficina_os_service.application.OrdemServicoService
import com.oficina_os_service.domain.enum.StatusOS
import com.oficina_os_service.infra.messaging.config.RabbitMQConfig
import com.oficina_os_service.infra.messaging.events.ExecucaoFinalizadaEvent
import com.oficina_os_service.infra.messaging.events.ExecucaoIniciadaEvent
import com.oficina_os_service.infra.nosql.OsResumoRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ExecutionEventConsumer(
  private val osService: OrdemServicoService,
  private val resumoRepository: OsResumoRepository
) {
  
  private val log = LoggerFactory.getLogger(ExecutionEventConsumer::class.java)
  
  @RabbitListener(queues = [RabbitMQConfig.QUEUE_EXECUCAO_INICIADA])
  fun onExecucaoIniciada(event: ExecucaoIniciadaEvent) {
    log.info("Recebido execucao.iniciada osId={} execucaoId={}", event.osId, event.execucaoId)
    resumoRepository.findById(event.osId).ifPresent { resumo ->
      resumoRepository.save(
        resumo.copy(
          execucaoId = event.execucaoId,
          statusExecucao = "EM_REPARO",
          ultimaAtualizacao = Instant.now()
        )
      )
    }
    osService.atualizarStatus(event.osId, StatusOS.EM_EXECUCAO, "Execução iniciada pelo Execution Service")
  }
  
  @RabbitListener(queues = [RabbitMQConfig.QUEUE_EXECUCAO_FINALIZADA])
  fun onExecucaoFinalizada(event: ExecucaoFinalizadaEvent) {
    log.info("Recebido execucao.finalizada osId={} execucaoId={}", event.osId, event.execucaoId)
    resumoRepository.findById(event.osId).ifPresent { resumo ->
      resumoRepository.save(
        resumo.copy(
          statusExecucao = "FINALIZADO",
          ultimaAtualizacao = Instant.now()
        )
      )
    }
    osService.atualizarStatus(event.osId, StatusOS.FINALIZADA, "Execução finalizada")
  }
}
