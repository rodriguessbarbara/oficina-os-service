package com.oficina_os_service.infra.dto

import com.oficina_os_service.domain.enum.StatusOS
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.Instant

data class CriarOrdemRequest(
  @NotNull val clienteId: Long,
  @NotNull val veiculoId: Long,
  @Valid val itensServico: List<ItemServicoRequest> = emptyList(),
  @Valid val itensEstoque: List<ItemEstoqueRequest> = emptyList()
)

data class ItemServicoRequest(
  @NotNull @Positive val servicoId: Long,
  @NotNull @Positive val quantidade: Int
)

data class ItemEstoqueRequest(
  @NotNull @Positive val estoqueId: Long,
  @NotNull @Positive val quantidade: BigDecimal,
  @NotNull @Positive val precoUnitario: BigDecimal
)

data class AtualizarStatusRequest(
  @NotNull val novoStatus: StatusOS,
  val motivo: String? = null
)

data class OrdemServicoResponse(
  val id: Long,
  val clienteId: Long,
  val veiculoId: Long,
  val status: StatusOS,
  val dataAbertura: Instant,
  val dataEncerramento: Instant?,
  val orcamento: OrcamentoResumo?,
  val pagamento: PagamentoResumo?,
  val execucao: ExecucaoResumo?,
  val historico: List<HistoricoStatusResponse>
)

data class OrcamentoResumo(
  val orcamentoId: String?,
  val valor: BigDecimal?,
  val status: String?
)

data class PagamentoResumo(
  val pagamentoId: String?,
  val status: String?
)

data class ExecucaoResumo(
  val execucaoId: Long?,
  val status: String?
)

data class HistoricoStatusResponse(
  val statusAnterior: StatusOS?,
  val statusNovo: StatusOS,
  val dataAlteracao: Instant,
  val motivo: String?
)