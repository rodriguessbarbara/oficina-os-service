package com.oficina_os_service.domain.model

import com.oficina_os_service.domain.enum.StatusOS
import java.math.BigDecimal
import java.time.Instant
import java.util.Collections.emptyList

data class OrdemServico(
  val id: Long? = null,
  val clienteId: Long,
  val veiculoId: Long,
  var status: StatusOS = StatusOS.RECEBIDA,
  var itensServico: List<ItemServico> = emptyList(),
  var itensEstoque: List<ItemEstoque> = emptyList(),
  val dataAbertura: Instant = Instant.now(),
  var dataEncerramento: Instant? = null,
  var inicioExecucao: Instant? = null,
  var fimExecucao: Instant? = null,
  var isAprovado: Boolean = false,
  var dataAprovacao: Instant? = null
)