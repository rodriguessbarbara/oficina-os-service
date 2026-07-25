package com.oficina_os_service.bdd

import com.oficina_os_service.infra.messaging.OsEventPublisher
import com.oficina_os_service.infra.nosql.HistoricoStatusRepository
import com.oficina_os_service.infra.nosql.OsResumoRepository
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = [
  "spring.autoconfigure.exclude[0]=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration",
  "spring.autoconfigure.exclude[1]=org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
  "spring.autoconfigure.exclude[2]=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration"
])
class CucumberConfig {
  
  // Mocks para infraestrutura não disponível no contexto de testes
  @MockBean lateinit var osEventPublisher: OsEventPublisher
  @MockBean lateinit var historicoStatusRepository: HistoricoStatusRepository
  @MockBean lateinit var osResumoRepository: OsResumoRepository
  @MockBean lateinit var mongoTemplate: MongoTemplate
}