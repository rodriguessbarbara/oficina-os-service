package com.oficina_os_service.domain.model

import java.math.BigDecimal

data class ItemEstoque(
  val id: Long? = null,
  val estoqueId: Long,
  val quantidade: BigDecimal,
  val precoUnitario: BigDecimal
)