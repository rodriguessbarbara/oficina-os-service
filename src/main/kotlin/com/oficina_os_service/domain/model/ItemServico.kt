package com.oficina_os_service.domain.model

import com.oficina_os_service.domain.enum.StatusItemServico
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant

data class ItemServico(
  val id: Long? = null,
  val servicoId: Long,
  val precoAplicado: BigDecimal,
  val quantidade: Int,
  var status: StatusItemServico = StatusItemServico.PENDENTE,
  var inicioExecucao: Instant? = null,
  var fimExecucao: Instant? = null
) {
  fun getDuracaoEmMinutos(): Long {
    if (status != StatusItemServico.FINALIZADO || inicioExecucao == null || fimExecucao == null) {
      return 0
    }
    return Duration.between(inicioExecucao, fimExecucao).toMinutes()
  }
}