package com.oficina_os_service.infra.repository

import com.oficina_os_service.domain.enum.StatusOS
import jakarta.persistence.*
import java.time.Instant
import java.util.Collections.emptyList

@Entity
@Table(name = "tb_ordem_servico")
class OrdemServicoEntity(
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,
  
  @Column(name = "cliente_id", nullable = false)
  val clienteId: Long,
  
  @Column(name = "veiculo_id", nullable = false)
  val veiculoId: Long,
  
  @Column(name = "orcamento_id")
  val orcamentoId: Long? = null,
  
  @Enumerated(EnumType.STRING)
  var status: StatusOS,
  
  @Column(name = "data_abertura", nullable = false)
  val dataAbertura: Instant,
  
  @Column(name = "data_encerramento")
  var dataEncerramento: Instant? = null,
  
  @Column(name = "inicio_execucao")
  var inicioExecucao: Instant? = null,
  
  @Column(name = "fim_execucao")
  var fimExecucao: Instant? = null,
  
  @Column(name = "is_aprovado", nullable = false)
  var isAprovado: Boolean = false,
  
  @Column(name = "data_aprovacao")
  var dataAprovacao: Instant? = null,
  
  @OneToMany(mappedBy = "ordemServico", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  var itensServico: List<ItemServicoEntity> = emptyList(),
  
  @OneToMany(mappedBy = "ordemServico", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  var itensEstoque: List<ItemEstoqueEntity> = emptyList()
)