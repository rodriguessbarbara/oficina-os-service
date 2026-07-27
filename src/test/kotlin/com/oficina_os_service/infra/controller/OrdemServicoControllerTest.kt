package com.oficina_os_service.infra.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.oficina_os_service.application.OrdemServicoService
import com.oficina_os_service.domain.enum.StatusOS
import com.oficina_os_service.infra.dto.AtualizarStatusRequest
import com.oficina_os_service.infra.dto.CriarOrdemRequest
import com.oficina_os_service.infra.dto.OrdemServicoResponse
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant

@WebMvcTest(OrdemServicoController::class)
class OrdemServicoControllerTest {
  
  @Autowired lateinit var mockMvc: MockMvc
  @Autowired lateinit var objectMapper: ObjectMapper
  @MockBean lateinit var service: OrdemServicoService
  
  private val baseResponse = OrdemServicoResponse(
    id = 1L, clienteId = 10L, veiculoId = 20L,
    status = StatusOS.RECEBIDA,
    dataAbertura = Instant.now(), dataEncerramento = null,
    orcamento = null, pagamento = null, execucao = null,
    historico = emptyList()
  )
  
  @Test
  fun `POST ordens deve retornar 201 com OS criada`() {
    val request = CriarOrdemRequest(clienteId = 10L, veiculoId = 20L)
    `when`(service.criar(request)).thenReturn(baseResponse)
    
    mockMvc.perform(
      post("/ordens")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
    )
      .andExpect(status().isCreated)
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.status").value("RECEBIDA"))
  }
  
  @Test
  fun `POST ordens deve retornar 400 quando clienteId nulo`() {
    val invalidRequest = """{"veiculoId": 20}"""
    
    mockMvc.perform(
      post("/ordens")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidRequest)
    )
      .andExpect(status().isBadRequest)
  }
  
  @Test
  fun `GET ordens_id deve retornar 200 com OS`() {
    `when`(service.consultar(1L)).thenReturn(baseResponse)
    
    mockMvc.perform(get("/ordens/1"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.clienteId").value(10))
  }
  
  @Test
  fun `GET ordens_id deve retornar 404 quando OS nao existe`() {
    `when`(service.consultar(999L)).thenThrow(EntityNotFoundException("OS id=999 não encontrada"))
    
    mockMvc.perform(get("/ordens/999"))
      .andExpect(status().isNotFound)
  }
  
  @Test
  fun `PATCH ordens_id_status deve retornar 200 com status atualizado`() {
    val request = AtualizarStatusRequest(novoStatus = StatusOS.AGUARDANDO_APROVACAO)
    val updated = baseResponse.copy(status = StatusOS.AGUARDANDO_APROVACAO)
    `when`(service.atualizarStatus(1L, request)).thenReturn(updated)
    
    mockMvc.perform(
      patch("/ordens/1/status")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
  }
  
  @Test
  fun `PATCH ordens_id_status deve retornar 400 quando novoStatus nulo`() {
    val invalidRequest = """{}"""
    
    mockMvc.perform(
      patch("/ordens/1/status")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidRequest)
    )
      .andExpect(status().isBadRequest)
  }
  
  @Test
  fun `DELETE ordens_id deve retornar 204`() {
    doNothing().`when`(service).cancelar(1L, null)
    
    mockMvc.perform(delete("/ordens/1"))
      .andExpect(status().isNoContent)
  }
  
  @Test
  fun `GET ordens deve listar por clienteId`() {
    `when`(service.listarPorCliente(10L)).thenReturn(listOf(baseResponse))
    
    mockMvc.perform(get("/ordens").param("clienteId", "10"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].id").value(1))
  }
}
