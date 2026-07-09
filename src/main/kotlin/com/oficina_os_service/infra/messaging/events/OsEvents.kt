package com.oficina_os_service.infra.messaging.events

// Eventos PUBLICADOS pelo OS Service
data class OsCriadaEvent(
  val osId: Long,
  val clienteId: Long,
  val veiculoId: Long,
  val itensServico: List<ItemServicoEventDto>,
  val itensEstoque: List<ItemEstoqueEventDto>
)

data class OsCanceladaEvent(
  val osId: Long,
  val motivo: String?
)

// Eventos publicados por Billing
data class OrcamentoGeradoEvent(
  val osId: Long,
  val orcamentoId: String,
  val valorTotal: java.math.BigDecimal
)

data class OrcamentoAprovadoEvent(
  val osId: Long,
  val orcamentoId: String
)

data class OrcamentoRejeitadoEvent(
  val osId: Long,
  val motivo: String?
)

data class PagamentoConfirmadoEvent(
  val osId: Long,
  val pagamentoId: String
)

data class PagamentoFalhouEvent(
  val osId: Long,
  val motivo: String?
)

// Eventos publicados por Execution
data class ExecucaoIniciadaEvent(
  val osId: Long,
  val execucaoId: Long
)

data class ExecucaoFinalizadaEvent(
  val osId: Long,
  val execucaoId: Long
)

data class ItemServicoEventDto(val servicoId: Long, val quantidade: Int, val precoAplicado: java.math.BigDecimal)
data class ItemEstoqueEventDto(val estoqueId: Long, val quantidade: java.math.BigDecimal, val precoUnitario: java.math.BigDecimal)
