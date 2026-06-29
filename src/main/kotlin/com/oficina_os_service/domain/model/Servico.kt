package com.oficina_os_service.domain.model

import java.math.BigDecimal

data class Servico(
  val id: Long? = null,
  val nome: String,
  val descricao: String,
  val preco: BigDecimal
)