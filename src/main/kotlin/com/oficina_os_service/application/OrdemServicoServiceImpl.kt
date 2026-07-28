package com.oficina_os_service.application

import com.oficina_os_service.domain.enum.StatusItemServico
import com.oficina_os_service.domain.enum.StatusOS
import com.oficina_os_service.infra.dto.*
import com.oficina_os_service.infra.messaging.OsEventPublisher
import com.oficina_os_service.infra.nosql.*
import com.oficina_os_service.infra.repository.*
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class OrdemServicoServiceImpl(
  private val osRepository: OrdemServicoJpaRepository,
  private val servicoRepository: ServicoJpaRepository,
  private val historicoRepository: HistoricoStatusRepository,
  private val resumoRepository: OsResumoRepository,
  private val publisher: OsEventPublisher
) : OrdemServicoService {
  
  override fun criar(request: CriarOrdemRequest): OrdemServicoResponse {
    val osEntity = OrdemServicoEntity(
      clienteId = request.clienteId,
      veiculoId = request.veiculoId,
      status = StatusOS.RECEBIDA,
      dataAbertura = Instant.now(),
      isAprovado = false
    )
    val savedOs = osRepository.save(osEntity)
    
    val itensServico = request.itensServico.map { item ->
      val servico = servicoRepository.findById(item.servicoId)
        .orElseThrow { EntityNotFoundException("Serviço id=${item.servicoId} não encontrado no catálogo") }
      ItemServicoEntity(
        servicoId = item.servicoId,
        precoAplicado = servico.preco,
        quantidade = item.quantidade,
        status = StatusItemServico.PENDENTE,
        ordemServico = savedOs
      )
    }.toMutableList()
    
    val itensEstoque = request.itensEstoque.map { item ->
      ItemEstoqueEntity(
        estoqueId = item.estoqueId,
        quantidade = item.quantidade,
        precoUnitario = item.precoUnitario,
        ordemServico = savedOs
      )
    }.toMutableList()
    
    savedOs.itensServico = itensServico
    savedOs.itensEstoque = itensEstoque
    val finalOs = osRepository.save(savedOs)
    val osId = finalOs.id!!
    
    resumoRepository.save(OsResumoDocument(osId = osId))
    historicoRepository.save(
      HistoricoStatusDocument(
        osId = osId,
        statusAnterior = null,
        statusNovo = StatusOS.RECEBIDA
      )
    )
    
    publisher.publicarOsCriada(finalOs)
    
    return finalOs.toResponse(emptyList(), null)
  }
  
  override fun atualizarStatus(id: Long, request: AtualizarStatusRequest): OrdemServicoResponse =
    atualizarStatus(id, request.novoStatus, request.motivo)
  
  override fun atualizarStatus(id: Long, novoStatus: StatusOS, motivo: String?): OrdemServicoResponse {
    val entity = osRepository.findById(id)
      .orElseThrow { EntityNotFoundException("OS id=$id não encontrada") }
    
    val statusAnterior = entity.status
    entity.status = novoStatus
    if (novoStatus == StatusOS.FINALIZADA || novoStatus == StatusOS.CANCELADA) {
      entity.dataEncerramento = Instant.now()
    }
    
    val saved = osRepository.save(entity)
    historicoRepository.save(
      HistoricoStatusDocument(
        osId = id,
        statusAnterior = statusAnterior,
        statusNovo = novoStatus,
        motivo = motivo
      )
    )
    
    val historico = historicoRepository.findAllByOsIdOrderByDataAlteracaoDesc(id)
    val resumo = resumoRepository.findById(id).orElse(null)
    return saved.toResponse(historico, resumo)
  }
  
  @Transactional(readOnly = true)
  override fun consultar(id: Long): OrdemServicoResponse {
    val entity = osRepository.findById(id)
      .orElseThrow { EntityNotFoundException("OS id=$id não encontrada") }
    val historico = historicoRepository.findAllByOsIdOrderByDataAlteracaoDesc(id)
    val resumo = resumoRepository.findById(id).orElse(null)
    return entity.toResponse(historico, resumo)
  }
  
  @Transactional(readOnly = true)
  override fun listarPorCliente(clienteId: Long): List<OrdemServicoResponse> =
    osRepository.findAllByClienteId(clienteId).map { entity ->
      val osId = entity.id!!
      val historico = historicoRepository.findAllByOsIdOrderByDataAlteracaoDesc(osId)
      val resumo = resumoRepository.findById(osId).orElse(null)
      entity.toResponse(historico, resumo)
    }
  
  override fun cancelar(id: Long, motivo: String?) {
    val entity = osRepository.findById(id)
      .orElseThrow { EntityNotFoundException("OS id=$id não encontrada") }
    
    val statusAnterior = entity.status
    entity.status = StatusOS.CANCELADA
    entity.dataEncerramento = Instant.now()
    osRepository.save(entity)
    
    historicoRepository.save(
      HistoricoStatusDocument(
        osId = id,
        statusAnterior = statusAnterior,
        statusNovo = StatusOS.CANCELADA,
        motivo = motivo ?: "Cancelamento via Saga"
      )
    )
    
    publisher.publicarOsCancelada(id, motivo)
  }
  
  // Mapper: OrdemServicoEntity → OrdemServicoResponse
  private fun OrdemServicoEntity.toResponse(
    historico: List<HistoricoStatusDocument>,
    resumo: OsResumoDocument?
  ) = OrdemServicoResponse(
    id = id!!,
    clienteId = clienteId,
    veiculoId = veiculoId,
    status = status,
    dataAbertura = dataAbertura,
    dataEncerramento = dataEncerramento,
    orcamento = resumo?.let {
      OrcamentoResumo(it.orcamentoId, it.valorOrcamento, it.statusOrcamento)
    },
    pagamento = resumo?.let {
      PagamentoResumo(it.pagamentoId, it.statusPagamento)
    },
    execucao = resumo?.let {
      ExecucaoResumo(it.execucaoId, it.statusExecucao)
    },
    historico = historico.map {
      HistoricoStatusResponse(it.statusAnterior, it.statusNovo, it.dataAlteracao, it.motivo)
    }
  )
}
