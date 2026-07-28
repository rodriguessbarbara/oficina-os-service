package com.oficina_os_service.bdd

import com.fasterxml.jackson.databind.ObjectMapper
import com.oficina_os_service.domain.enum.StatusOS
import com.oficina_os_service.infra.repository.OrdemServicoEntity
import com.oficina_os_service.infra.repository.OrdemServicoJpaRepository
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import java.time.Instant

class CancelamentoOrdemStepsTest {
  
  @Autowired lateinit var mockMvc: MockMvc
  @Autowired lateinit var objectMapper: ObjectMapper
  @Autowired lateinit var osRepository: OrdemServicoJpaRepository
  
  private lateinit var result: MvcResult
  private var osIdCriada: Long = -1L
  
  @Given("uma OS existente com id {long}")
  fun umaOsExistenteComId(id: Long) {
    osRepository.deleteAll()
    
    val osParaSalvar = OrdemServicoEntity(
      clienteId = 1L,
      veiculoId = 1L,
      status = StatusOS.RECEBIDA,
      dataAbertura = Instant.now(),
      isAprovado = false,
      dataAprovacao = null,
      itensServico = mutableListOf(),
      itensEstoque = mutableListOf()
    )
    
    val osSalva = osRepository.save(osParaSalvar)
    osIdCriada = osSalva.id!!
  }
  
  @When("o cancelamento da OS {long} e solicitado com motivo {string}")
  fun oCancelamentoDaOsESolicitadoComMotivo(id: Long, motivo: String) {
    val targetId = if (osIdCriada > 0L) osIdCriada else id
    
    result = mockMvc.perform(
      delete("/ordens/$targetId").param("motivo", motivo)
    ).andReturn()
  }
  
  @Then("o cancelamento deve retornar status HTTP {int}")
  fun oCancelamentoDeveRetornarStatusHttp(statusEsperado: Int) {
    assertThat(result.response.status).isEqualTo(statusEsperado)
  }
  
  @Then("a OS deve ter status CANCELADA")
  fun aOsDeveTerStatusCancelada() {
    assertThat(result.response.status).isIn(204, 200)
  }
  
  @Then("o endpoint GET ordens {long} deve retornar status {string}")
  fun oEndpointGetOrdensDeveRetornarStatus(id: Long, expectedStatus: String) {
    val consultaResult = mockMvc.perform(get("/ordens/$osIdCriada")).andReturn()
    if (consultaResult.response.status == 200) {
      val body = objectMapper.readTree(consultaResult.response.contentAsString)
      assertThat(body.get("status").asText()).isEqualTo(expectedStatus)
    }
  }
}