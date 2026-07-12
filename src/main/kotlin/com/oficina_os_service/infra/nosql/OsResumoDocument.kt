package com.oficina_os_service.infra.nosql

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@Document(collection = "os_resumo")
data class OsResumoDocument(
  @Id val osId: Long,
  val orcamentoId: String? = null,
  val valorOrcamento: BigDecimal? = null,
  val statusOrcamento: String? = null,
  val pagamentoId: String? = null,
  val statusPagamento: String? = null,
  val execucaoId: Long? = null,
  val statusExecucao: String? = null,
  val ultimaAtualizacao: Instant = Instant.now()
)