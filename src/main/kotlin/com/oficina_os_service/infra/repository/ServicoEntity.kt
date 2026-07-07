package com.oficina_os_service.infra.repository

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "tb_servico")
class ServicoEntity(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,
  val nome: String,
  val descricao: String?,
  val preco: BigDecimal
)