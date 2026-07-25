package com.oficina_os_service.infra.messaging

import com.oficina_os_service.application.OrdemServicoService
import com.oficina_os_service.domain.enum.StatusOS
import com.oficina_os_service.infra.messaging.events.*
import com.oficina_os_service.infra.nosql.OsResumoDocument
import com.oficina_os_service.infra.nosql.OsResumoRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class BillingEventConsumerTest {
  
  @Mock lateinit var osService: OrdemServicoService
  @Mock lateinit var resumoRepository: OsResumoRepository
  
  @InjectMocks lateinit var consumer: BillingEventConsumer
  
  @Test
  fun `onOrcamentoGerado deve salvar resumo e atualizar status para AGUARDANDO_APROVACAO`() {
    val event = OrcamentoGeradoEvent(osId = 1L, orcamentoId = "orca-123", valorTotal = BigDecimal("500.00"))
    val resumo = OsResumoDocument(osId = 1L)
    `when`(resumoRepository.findById(1L)).thenReturn(Optional.of(resumo))
    `when`(resumoRepository.save(any(OsResumoDocument::class.java))).thenReturn(resumo)
    
    consumer.onOrcamentoGerado(event)
    
    val captor = ArgumentCaptor.forClass(OsResumoDocument::class.java)
    verify(resumoRepository).save(captor.capture())
    assertThat(captor.value.orcamentoId).isEqualTo("orca-123")
    assertThat(captor.value.statusOrcamento).isEqualTo("PENDENTE")
    verify(osService).atualizarStatus(1L, StatusOS.AGUARDANDO_APROVACAO, any())
  }
  
  @Test
  fun `onOrcamentoRejeitado deve cancelar OS via Saga`() {
    val event = OrcamentoRejeitadoEvent(osId = 1L, motivo = "Preço alto")
    `when`(resumoRepository.findById(1L)).thenReturn(Optional.empty())
    
    consumer.onOrcamentoRejeitado(event)
    
    verify(osService).cancelar(1L, "Preço alto")
  }
  
  @Test
  fun `onPagamentoConfirmado deve atualizar status para APROVADA`() {
    val event = PagamentoConfirmadoEvent(osId = 1L, pagamentoId = "pag-456")
    val resumo = OsResumoDocument(osId = 1L)
    `when`(resumoRepository.findById(1L)).thenReturn(Optional.of(resumo))
    `when`(resumoRepository.save(any(OsResumoDocument::class.java))).thenReturn(resumo)
    
    consumer.onPagamentoConfirmado(event)
    
    verify(osService).atualizarStatus(1L, StatusOS.APROVADA, any())
  }
  
  @Test
  fun `onPagamentoFalhou deve cancelar OS`() {
    val event = PagamentoFalhouEvent(osId = 1L, motivo = "Saldo insuficiente")
    `when`(resumoRepository.findById(1L)).thenReturn(Optional.empty())
    
    consumer.onPagamentoFalhou(event)
    
    verify(osService).cancelar(1L, "Saldo insuficiente")
  }
  
  @Test
  fun `onOrcamentoAprovado deve atualizar status para APROVADA`() {
    val event = OrcamentoAprovadoEvent(osId = 1L, orcamentoId = "orca-123")
    val resumo = OsResumoDocument(osId = 1L)
    `when`(resumoRepository.findById(1L)).thenReturn(Optional.of(resumo))
    `when`(resumoRepository.save(any(OsResumoDocument::class.java))).thenReturn(resumo)
    
    consumer.onOrcamentoAprovado(event)
    
    verify(osService).atualizarStatus(1L, StatusOS.APROVADA, any())
  }
}