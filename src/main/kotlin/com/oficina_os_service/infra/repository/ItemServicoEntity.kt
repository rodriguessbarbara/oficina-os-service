package com.oficina_os_service.infra.repository

import com.oficina_os_service.domain.enum.StatusItemServico
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "tb_item_servico")
class ItemServicoEntity(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,
  
  @Column(name = "servico_id", nullable = false)
  val servicoId: Long,
  
  @Column(name = "preco_aplicado", nullable = false)
  val precoAplicado: BigDecimal,
  
  val quantidade: Int,
  
  @Enumerated(EnumType.STRING)
  var status: StatusItemServico,
  
  @Column(name = "inicio_execucao")
  var inicioExecucao: Instant? = null,
  
  @Column(name = "fim_execucao")
  var fimExecucao: Instant? = null,
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ordem_servico_id", nullable = false)
  val ordemServico: OrdemServicoEntity
)