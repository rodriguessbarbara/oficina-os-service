package com.oficina_os_service.infra.messaging

import com.oficina_os_service.application.OrdemServicoService
import com.oficina_os_service.domain.enum.StatusOS
import com.oficina_os_service.infra.messaging.events.ExecucaoFinalizadaEvent
import com.oficina_os_service.infra.messaging.events.ExecucaoIniciadaEvent
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
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ExecutionEventConsumerTest {
  
  @Mock lateinit var osService: OrdemServicoService
  @Mock lateinit var resumoRepository: OsResumoRepository
  
  @InjectMocks lateinit var consumer: ExecutionEventConsumer
  
  @Test
  fun `onExecucaoIniciada deve atualizar resumo e status para EM_EXECUCAO`() {
    val event = ExecucaoIniciadaEvent(osId = 1L, execucaoId = 100L)
    val resumo = OsResumoDocument(osId = 1L)
    `when`(resumoRepository.findById(1L)).thenReturn(Optional.of(resumo))
    `when`(resumoRepository.save(any(OsResumoDocument::class.java))).thenReturn(resumo)
    
    consumer.onExecucaoIniciada(event)
    
    val captor = ArgumentCaptor.forClass(OsResumoDocument::class.java)
    verify(resumoRepository).save(captor.capture())
    assertThat(captor.value.execucaoId).isEqualTo(100L)
    assertThat(captor.value.statusExecucao).isEqualTo("EM_REPARO")
    verify(osService).atualizarStatus(1L, StatusOS.EM_EXECUCAO, any())
  }
  
  @Test
  fun `onExecucaoFinalizada deve atualizar resumo e status para FINALIZADA`() {
    val event = ExecucaoFinalizadaEvent(osId = 1L, execucaoId = 100L)
    val resumo = OsResumoDocument(osId = 1L, execucaoId = 100L, statusExecucao = "EM_REPARO")
    `when`(resumoRepository.findById(1L)).thenReturn(Optional.of(resumo))
    `when`(resumoRepository.save(any(OsResumoDocument::class.java))).thenReturn(resumo)
    
    consumer.onExecucaoFinalizada(event)
    
    val captor = ArgumentCaptor.forClass(OsResumoDocument::class.java)
    verify(resumoRepository).save(captor.capture())
    assertThat(captor.value.statusExecucao).isEqualTo("FINALIZADO")
    verify(osService).atualizarStatus(1L, StatusOS.FINALIZADA, any())
  }
  
  @Test
  fun `onExecucaoIniciada nao deve falhar quando resumo nao existe`() {
    val event = ExecucaoIniciadaEvent(osId = 1L, execucaoId = 100L)
    `when`(resumoRepository.findById(1L)).thenReturn(Optional.empty())
    
    consumer.onExecucaoIniciada(event)
    
    verify(resumoRepository, never()).save(any())
    verify(osService).atualizarStatus(1L, StatusOS.EM_EXECUCAO, any())
  }
}
