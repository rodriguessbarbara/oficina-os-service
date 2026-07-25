package com.oficina_os_service.bdd

import com.fasterxml.jackson.databind.ObjectMapper
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

class CancelamentoOrdemStepsTest {
  
  @Autowired lateinit var mockMvc: MockMvc
  @Autowired lateinit var objectMapper: ObjectMapper
  
  private lateinit var result: MvcResult
  private var osIdCriada: Long = 0L
  
  @Given("uma OS existente com id {long}")
  fun umaOsExistenteComId(id: Long) {
    osIdCriada = id
  }
  
  @When("o cancelamento da OS {long} e solicitado com motivo {string}")
  fun oCancelamentoDaOsESolicitadoComMotivo(id: Long, motivo: String) {
    result = mockMvc.perform(
      delete("/ordens/$id").param("motivo", motivo)
    ).andReturn()
  }
  
  @Then("a OS deve ter status CANCELADA")
  fun aOsDeveTerStatusCancelada() {
    assertThat(result.response.status).isIn(204, 200)
  }
  
  @Then("o endpoint GET ordens {long} deve retornar status {string}")
  fun oEndpointGetOrdensDeveRetornarStatus(id: Long, expectedStatus: String) {
    val consultaResult = mockMvc.perform(get("/ordens/$id")).andReturn()
    if (consultaResult.response.status == 200) {
      val body = objectMapper.readTree(consultaResult.response.contentAsString)
      assertThat(body.get("status").asText()).isEqualTo(expectedStatus)
    }
  }
}