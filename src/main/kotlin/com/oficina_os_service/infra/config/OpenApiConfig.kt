package com.oficina_os_service.infra.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

  @Bean
  fun openApi(): OpenAPI = OpenAPI()
    .info(
      Info()
        .title("OS Service API")
        .version("1.0.0")
        .description(
          """
          Microsserviço responsável pelo ciclo de vida das Ordens de Serviço.

          **Saga Coreografada:**
          - Publica `os.criada` ao abrir uma OS
          - Consome `orcamento.gerado`, `orcamento.aprovado/rejeitado` do Billing Service
          - Consome `pagamento.confirmado/falhou` do Billing Service
          - Consome `execucao.iniciada/finalizada` do Execution Service
          - Publica `os.cancelada` em transações compensatórias
          """.trimIndent()
        )
        .contact(Contact().name("Oficina Mecânica – Tech Challenge Fase 4"))
    )
}