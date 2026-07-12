package com.oficina_os_service.infra.nosql

import com.oficina_os_service.domain.enum.StatusOS
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "historico_status")
data class HistoricoStatusDocument(
  @Id val id: String? = null,
  @Indexed val osId: Long,
  val statusAnterior: StatusOS?,
  val statusNovo: StatusOS,
  val dataAlteracao: Instant = Instant.now(),
  val motivo: String? = null
)