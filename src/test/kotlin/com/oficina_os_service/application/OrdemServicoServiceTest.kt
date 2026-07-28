package com.oficina_os_service.application

import com.oficina_os_service.domain.enum.StatusOS
import com.oficina_os_service.infra.dto.AtualizarStatusRequest
import com.oficina_os_service.infra.dto.CriarOrdemRequest
import com.oficina_os_service.infra.dto.ItemEstoqueRequest
import com.oficina_os_service.infra.dto.ItemServicoRequest
import com.oficina_os_service.infra.messaging.OsEventPublisher
import com.oficina_os_service.infra.nosql.*
import com.oficina_os_service.infra.repository.*
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class OrdemServicoServiceTest {
  
  @Mock lateinit var osRepository: OrdemServicoJpaRepository
  @Mock lateinit var servicoRepository: ServicoJpaRepository
  @Mock lateinit var historicoRepository: HistoricoStatusRepository
  @Mock lateinit var resumoRepository: OsResumoRepository
  @Mock lateinit var publisher: OsEventPublisher
  
  @InjectMocks lateinit var service: OrdemServicoServiceImpl
  
  private lateinit var osEntity: OrdemServicoEntity
  private lateinit var servicoEntity: ServicoEntity
  
  @BeforeEach
  fun setup() {
    osEntity = OrdemServicoEntity(
      id = 1L,
      clienteId = 10L,
      veiculoId = 20L,
      status = StatusOS.RECEBIDA,
      dataAbertura = Instant.now(),
      isAprovado = false
    )
    servicoEntity = ServicoEntity(id = 5L, nome = "Troca de óleo", descricao = "Serviço padrão", preco = BigDecimal("150.00"))
  }
  
  @Test
  fun `criar deve salvar OS e publicar evento os_criada`() {
    val request = CriarOrdemRequest(
      clienteId = 10L,
      veiculoId = 20L,
      itensServico = listOf(ItemServicoRequest(servicoId = 5L, quantidade = 1)),
      itensEstoque = emptyList()
    )
    
    `when`(osRepository.save(any(OrdemServicoEntity::class.java))).thenReturn(osEntity)
    `when`(servicoRepository.findById(5L)).thenReturn(Optional.of(servicoEntity))
    `when`(resumoRepository.save(any(OsResumoDocument::class.java))).thenReturn(OsResumoDocument(osId = 1L))
    `when`(historicoRepository.save(any(HistoricoStatusDocument::class.java)))
      .thenReturn(HistoricoStatusDocument(osId = 1L, statusAnterior = null, statusNovo = StatusOS.RECEBIDA))
    
    val response = service.criar(request)
    
    assertThat(response.status).isEqualTo(StatusOS.RECEBIDA)
    assertThat(response.clienteId).isEqualTo(10L)
    verify(publisher).publicarOsCriada(osEntity)
    verify(historicoRepository).save(any(HistoricoStatusDocument::class.java))
  }
  
  @Test
  fun `criar deve lancar EntityNotFoundException quando servico nao existe`() {
    val request = CriarOrdemRequest(
      clienteId = 10L, veiculoId = 20L,
      itensServico = listOf(ItemServicoRequest(servicoId = 99L, quantidade = 1))
    )
    `when`(osRepository.save(any(OrdemServicoEntity::class.java))).thenReturn(osEntity)
    `when`(servicoRepository.findById(99L)).thenReturn(Optional.empty())
    
    assertThatThrownBy { service.criar(request) }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessageContaining("99")
  }

  @Test
  fun `criar deve associar itens de estoque a OS`() {
    val request = CriarOrdemRequest(
      clienteId = 10L,
      veiculoId = 20L,
      itensEstoque = listOf(
        ItemEstoqueRequest(
          estoqueId = 8L,
          quantidade = BigDecimal("3.00"),
          precoUnitario = BigDecimal("25.50")
        )
      )
    )
    `when`(osRepository.save(any(OrdemServicoEntity::class.java))).thenReturn(osEntity)

    service.criar(request)

    assertThat(osEntity.itensEstoque).hasSize(1)
    val itemEstoque = osEntity.itensEstoque.single()
    assertThat(itemEstoque.estoqueId).isEqualTo(8L)
    assertThat(itemEstoque.quantidade).isEqualByComparingTo("3.00")
    assertThat(itemEstoque.precoUnitario).isEqualByComparingTo("25.50")
    assertThat(itemEstoque.ordemServico).isSameAs(osEntity)
  }
  
  @Test
  fun `consultar deve retornar OS com historico e resumo`() {
    val historico = listOf(
      HistoricoStatusDocument(osId = 1L, statusAnterior = null, statusNovo = StatusOS.RECEBIDA)
    )
    val resumo = OsResumoDocument(osId = 1L, statusOrcamento = "PENDENTE")
    
    `when`(osRepository.findById(1L)).thenReturn(Optional.of(osEntity))
    `when`(historicoRepository.findAllByOsIdOrderByDataAlteracaoDesc(1L)).thenReturn(historico)
    `when`(resumoRepository.findById(1L)).thenReturn(Optional.of(resumo))
    
    val response = service.consultar(1L)
    
    assertThat(response.id).isEqualTo(1L)
    assertThat(response.historico).hasSize(1)
    assertThat(response.orcamento?.status).isEqualTo("PENDENTE")
  }
  
  @Test
  fun `consultar deve lancar EntityNotFoundException quando OS nao existe`() {
    `when`(osRepository.findById(999L)).thenReturn(Optional.empty())
    
    assertThatThrownBy { service.consultar(999L) }
      .isInstanceOf(EntityNotFoundException::class.java)
  }
  
  @Test
  fun `atualizarStatus deve mudar status e salvar historico`() {
    val request = AtualizarStatusRequest(novoStatus = StatusOS.AGUARDANDO_APROVACAO)
    val updated = osEntity.apply { status = StatusOS.AGUARDANDO_APROVACAO }
    
    `when`(osRepository.findById(1L)).thenReturn(Optional.of(osEntity))
    `when`(osRepository.save(any(OrdemServicoEntity::class.java))).thenReturn(updated)
    `when`(historicoRepository.save(any())).thenReturn(
      HistoricoStatusDocument(osId = 1L, statusAnterior = StatusOS.RECEBIDA, statusNovo = StatusOS.AGUARDANDO_APROVACAO)
    )
    `when`(historicoRepository.findAllByOsIdOrderByDataAlteracaoDesc(1L)).thenReturn(emptyList())
    `when`(resumoRepository.findById(1L)).thenReturn(Optional.empty())
    
    val response = service.atualizarStatus(1L, request)
    
    assertThat(response.status).isEqualTo(StatusOS.AGUARDANDO_APROVACAO)
    verify(historicoRepository).save(any(HistoricoStatusDocument::class.java))
  }

  @Test
  fun `atualizarStatus deve lancar EntityNotFoundException quando OS nao existe`() {
    `when`(osRepository.findById(999L)).thenReturn(Optional.empty())

    assertThatThrownBy {
      service.atualizarStatus(999L, StatusOS.EM_EXECUCAO, "Execução iniciada")
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessageContaining("999")
  }

  @ParameterizedTest
  @EnumSource(
    value = StatusOS::class,
    names = ["FINALIZADA", "CANCELADA"]
  )
  fun `atualizarStatus deve encerrar OS em status terminal`(statusTerminal: StatusOS) {
    `when`(osRepository.findById(1L)).thenReturn(Optional.of(osEntity))
    `when`(osRepository.save(any(OrdemServicoEntity::class.java))).thenAnswer { it.arguments[0] }
    `when`(historicoRepository.findAllByOsIdOrderByDataAlteracaoDesc(1L)).thenReturn(emptyList())
    `when`(resumoRepository.findById(1L)).thenReturn(Optional.empty())

    val response = service.atualizarStatus(1L, statusTerminal, "Transição terminal")

    assertThat(response.status).isEqualTo(statusTerminal)
    assertThat(response.dataEncerramento).isNotNull()
  }
  
  @Test
  fun `cancelar deve mudar status para CANCELADA e publicar os_cancelada`() {
    `when`(osRepository.findById(1L)).thenReturn(Optional.of(osEntity))
    `when`(osRepository.save(any(OrdemServicoEntity::class.java))).thenReturn(osEntity)
    `when`(historicoRepository.save(any())).thenReturn(
      HistoricoStatusDocument(osId = 1L, statusAnterior = StatusOS.RECEBIDA, statusNovo = StatusOS.CANCELADA)
    )
    
    service.cancelar(1L, "Rollback Saga")
    
    verify(publisher).publicarOsCancelada(1L, "Rollback Saga")
    verify(osRepository).save(any(OrdemServicoEntity::class.java))
  }
  
  @Test
  fun `cancelar deve definir dataEncerramento`() {
    `when`(osRepository.findById(1L)).thenReturn(Optional.of(osEntity))
    `when`(osRepository.save(any(OrdemServicoEntity::class.java))).thenAnswer { it.arguments[0] as OrdemServicoEntity }
    `when`(historicoRepository.save(any())).thenReturn(
      HistoricoStatusDocument(osId = 1L, statusAnterior = StatusOS.RECEBIDA, statusNovo = StatusOS.CANCELADA)
    )
    
    service.cancelar(1L, null)
    
    val entityCaptor = org.mockito.ArgumentCaptor.forClass(OrdemServicoEntity::class.java)
    verify(osRepository).save(entityCaptor.capture())
    assertThat(entityCaptor.value.dataEncerramento).isNotNull()
    assertThat(entityCaptor.value.status).isEqualTo(StatusOS.CANCELADA)
  }
  
  @Test
  fun `listarPorCliente deve retornar lista vazia quando nao ha OS`() {
    `when`(osRepository.findAllByClienteId(10L)).thenReturn(emptyList())
    
    val result = service.listarPorCliente(10L)
    
    assertThat(result).isEmpty()
  }

  @Test
  fun `listarPorCliente deve enriquecer cada OS com historico e resumo`() {
    val historico = HistoricoStatusDocument(
      osId = 1L,
      statusAnterior = StatusOS.RECEBIDA,
      statusNovo = StatusOS.AGUARDANDO_APROVACAO
    )
    val resumo = OsResumoDocument(
      osId = 1L,
      orcamentoId = "orca-123",
      statusOrcamento = "PENDENTE"
    )
    `when`(osRepository.findAllByClienteId(10L)).thenReturn(listOf(osEntity))
    `when`(historicoRepository.findAllByOsIdOrderByDataAlteracaoDesc(1L))
      .thenReturn(listOf(historico))
    `when`(resumoRepository.findById(1L)).thenReturn(Optional.of(resumo))

    val result = service.listarPorCliente(10L)

    assertThat(result).hasSize(1)
    val ordem = result.single()
    assertThat(ordem.id).isEqualTo(1L)
    assertThat(ordem.historico).hasSize(1)
    assertThat(ordem.orcamento?.orcamentoId).isEqualTo("orca-123")
    assertThat(ordem.orcamento?.status).isEqualTo("PENDENTE")
  }
}
