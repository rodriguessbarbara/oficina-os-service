package com.oficina_os_service.infra.client

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

class ServiceClientsTest {

  private lateinit var server: HttpServer
  private lateinit var baseUrl: String

  @BeforeEach
  fun iniciarServidor() {
    server = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)
    server.start()
    baseUrl = "http://${server.address.hostString}:${server.address.port}"
  }

  @AfterEach
  fun pararServidor() {
    server.stop(0)
  }

  @Test
  fun `Billing deve desserializar orcamento retornado pelo servico`() {
    server.createContext("/orcamentos/os/1") {
      it.respond(
        200,
        """{"orcamentoId":"orca-123","valor":500.00,"status":"PENDENTE"}"""
      )
    }
    val client = BillingServiceClient(baseUrl)

    val resumo = client.buscarOrcamentoPorOs(1L)

    assertThat(resumo?.orcamentoId).isEqualTo("orca-123")
    assertThat(resumo?.valor).isEqualByComparingTo("500.00")
    assertThat(resumo?.status).isEqualTo("PENDENTE")
  }

  @Test
  fun `Billing deve retornar nulo quando servico responder com erro`() {
    server.createContext("/orcamentos/os/1") { it.respond(503) }
    val client = BillingServiceClient(baseUrl)

    val resumo = client.buscarOrcamentoPorOs(1L)

    assertThat(resumo).isNull()
  }

  @Test
  fun `Execution deve desserializar execucao retornada pelo servico`() {
    server.createContext("/execucoes/os/1") {
      it.respond(200, """{"execucaoId":100,"status":"EM_REPARO"}""")
    }
    val client = ExecutionServiceClient(baseUrl)

    val resumo = client.buscarExecucaoPorOs(1L)

    assertThat(resumo?.execucaoId).isEqualTo(100L)
    assertThat(resumo?.status).isEqualTo("EM_REPARO")
  }

  @Test
  fun `Execution deve retornar nulo quando servico responder com erro`() {
    server.createContext("/execucoes/os/1") { it.respond(503) }
    val client = ExecutionServiceClient(baseUrl)

    val resumo = client.buscarExecucaoPorOs(1L)

    assertThat(resumo).isNull()
  }

  private fun HttpExchange.respond(status: Int, body: String = "") {
    val response = body.toByteArray(StandardCharsets.UTF_8)
    if (body.isNotEmpty()) {
      responseHeaders.add("Content-Type", "application/json")
      sendResponseHeaders(status, response.size.toLong())
      responseBody.use { it.write(response) }
    } else {
      sendResponseHeaders(status, -1)
    }
    close()
  }
}
