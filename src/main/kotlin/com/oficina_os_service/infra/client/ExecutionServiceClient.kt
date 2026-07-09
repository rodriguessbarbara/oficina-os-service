package com.oficina_os_service.infra.client

import com.oficina_os_service.infra.dto.ExecucaoResumo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ExecutionServiceClient(
  @Value("\${services.execution.url:http://execution-service}") executionUrl: String
) {
  private val log = LoggerFactory.getLogger(ExecutionServiceClient::class.java)
  private val client = RestClient.create(executionUrl)
  
  fun buscarExecucaoPorOs(osId: Long): ExecucaoResumo? =
    runCatching {
      client.get()
        .uri("/execucoes/os/{osId}", osId)
        .retrieve()
        .body(ExecucaoResumo::class.java)
    }.onFailure {
      log.warn("Falha ao buscar execução no Execution Service para OS id={}: {}", osId, it.message)
    }.getOrNull()
}