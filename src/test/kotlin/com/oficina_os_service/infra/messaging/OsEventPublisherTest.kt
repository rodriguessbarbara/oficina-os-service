package com.oficina_os_service.infra.messaging

import com.oficina_os_service.domain.enum.StatusItemServico
import com.oficina_os_service.domain.enum.StatusOS
import com.oficina_os_service.infra.messaging.config.RabbitMQConfig
import com.oficina_os_service.infra.messaging.events.OsCanceladaEvent
import com.oficina_os_service.infra.messaging.events.OsCriadaEvent
import com.oficina_os_service.infra.repository.ItemEstoqueEntity
import com.oficina_os_service.infra.repository.ItemServicoEntity
import com.oficina_os_service.infra.repository.OrdemServicoEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.eq
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.math.BigDecimal
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class OsEventPublisherTest {

  @Mock lateinit var rabbitTemplate: RabbitTemplate
  @InjectMocks lateinit var publisher: OsEventPublisher

  @Test
  fun `publicarOsCriada deve enviar evento com servicos e itens de estoque`() {
    val ordem = OrdemServicoEntity(
      id = 1L,
      clienteId = 10L,
      veiculoId = 20L,
      status = StatusOS.RECEBIDA,
      dataAbertura = Instant.now()
    )
    ordem.itensServico = mutableListOf(
      ItemServicoEntity(
        servicoId = 5L,
        precoAplicado = BigDecimal("150.00"),
        quantidade = 2,
        status = StatusItemServico.PENDENTE,
        ordemServico = ordem
      )
    )
    ordem.itensEstoque = mutableListOf(
      ItemEstoqueEntity(
        estoqueId = 8L,
        quantidade = BigDecimal("3.00"),
        precoUnitario = BigDecimal("25.50"),
        ordemServico = ordem
      )
    )

    publisher.publicarOsCriada(ordem)

    val eventCaptor = ArgumentCaptor.forClass(OsCriadaEvent::class.java)
    verify(rabbitTemplate).convertAndSend(
      eq(RabbitMQConfig.OS_EXCHANGE),
      eq(RabbitMQConfig.ROUTING_OS_CRIADA),
      eventCaptor.capture()
    )
    val event = eventCaptor.value
    assertThat(event.osId).isEqualTo(1L)
    assertThat(event.clienteId).isEqualTo(10L)
    assertThat(event.veiculoId).isEqualTo(20L)
    assertThat(event.itensServico).hasSize(1)
    val itemServico = event.itensServico.single()
    assertThat(itemServico.servicoId).isEqualTo(5L)
    assertThat(itemServico.quantidade).isEqualTo(2)
    assertThat(itemServico.precoAplicado).isEqualByComparingTo("150.00")
    assertThat(event.itensEstoque).hasSize(1)
    val itemEstoque = event.itensEstoque.single()
    assertThat(itemEstoque.estoqueId).isEqualTo(8L)
    assertThat(itemEstoque.quantidade).isEqualByComparingTo("3.00")
    assertThat(itemEstoque.precoUnitario).isEqualByComparingTo("25.50")
  }

  @Test
  fun `publicarOsCancelada deve enviar identificador e motivo`() {
    publisher.publicarOsCancelada(1L, "Peça indisponível")

    val eventCaptor = ArgumentCaptor.forClass(OsCanceladaEvent::class.java)
    verify(rabbitTemplate).convertAndSend(
      eq(RabbitMQConfig.OS_EXCHANGE),
      eq(RabbitMQConfig.ROUTING_OS_CANCELADA),
      eventCaptor.capture()
    )
    assertThat(eventCaptor.value.osId).isEqualTo(1L)
    assertThat(eventCaptor.value.motivo).isEqualTo("Peça indisponível")
  }
}
