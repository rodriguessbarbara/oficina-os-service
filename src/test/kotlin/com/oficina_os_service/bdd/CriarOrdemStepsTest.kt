package com.oficina_os_service.bdd

import com.fasterxml.jackson.databind.ObjectMapper
import com.oficina_os_service.infra.dto.CriarOrdemRequest
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

class CriarOrdemStepsTest {
  
  @Autowired lateinit var mockMvc: MockMvc
  @Autowired lateinit var objectMapper: ObjectMapper
  
  private lateinit var result: MvcResult
  
  @Given("um cliente com id {long} e veiculo com id {long}")
  fun umClienteComIdEVeiculoComId(clienteId: Long, veiculoId: Long) {
    CriarOrdemRequest(clienteId = clienteId, veiculoId = veiculoId)
  }
  
  @When("uma OS e criada para o cliente {long} e veiculo {long}")
  fun umaOsECriadaParaOClienteEVeiculo(clienteId: Long, veiculoId: Long) {
    val request = CriarOrdemRequest(clienteId = clienteId, veiculoId = veiculoId)
    result = mockMvc.perform(
      post("/ordens")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
    ).andReturn()
  }
  
  @Then("a OS deve ser criada com status {string}")
  fun aOsDeveSerCriadaComStatus(expectedStatus: String) {
    assertThat(result.response.status).isIn(201, 200)
    val body = objectMapper.readTree(result.response.contentAsString)
    assertThat(body.get("status").asText()).isEqualTo(expectedStatus)
  }
  
  @Then("a resposta deve ter status HTTP {int}")
  fun aRespostaDeveTerStatusHttp(httpStatus: Int) {
    assertThat(result.response.status).isEqualTo(httpStatus)
  }
}