package com.oficina_os_service.infra.client

import com.oficina_os_service.infra.dto.OrcamentoResumo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class BillingServiceClient(
  @Value("\${services.billing.url:http://billing-service}") billingUrl: String
) {
  private val log = LoggerFactory.getLogger(BillingServiceClient::class.java)
  private val client = RestClient.create(billingUrl)
  
  fun buscarOrcamentoPorOs(osId: Long): OrcamentoResumo? =
    runCatching {
      client.get()
        .uri("/orcamentos/os/{osId}", osId)
        .retrieve()
        .body(OrcamentoResumo::class.java)
    }.onFailure {
      log.warn("Falha ao buscar orçamento no Billing Service para OS id={}: {}", osId, it.message)
    }.getOrNull()
}
