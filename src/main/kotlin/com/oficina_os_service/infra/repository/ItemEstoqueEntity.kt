package com.oficina_os_service.infra.repository

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "tb_item_estoque")
class ItemEstoqueEntity(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,
  
  @Column(name = "estoque_id", nullable = false)
  val estoqueId: Long,
  
  val quantidade: BigDecimal,
  
  @Column(name = "preco_unitario", nullable = false)
  val precoUnitario: BigDecimal,
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ordem_servico_id", nullable = false)
  val ordemServico: OrdemServicoEntity
)