package com.oficina_os_service.domain.enum

enum class StatusOS(val prioridade: Int) {
  EM_EXECUCAO(1),
  AGUARDANDO_APROVACAO(2),
  EM_DIAGNOSTICO(3),
  RECEBIDA(4),
  APROVADA(5),
  CANCELADA(6),
  FINALIZADA(7),
  ENTREGUE(8)
}