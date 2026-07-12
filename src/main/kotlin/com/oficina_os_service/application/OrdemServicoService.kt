package com.oficina_os_service.application

import com.oficina_os_service.domain.enum.StatusOS
import com.oficina_os_service.infra.dto.AtualizarStatusRequest
import com.oficina_os_service.infra.dto.CriarOrdemRequest
import com.oficina_os_service.infra.dto.OrdemServicoResponse

interface OrdemServicoService {
  fun criar(request: CriarOrdemRequest): OrdemServicoResponse
  fun atualizarStatus(id: Long, request: AtualizarStatusRequest): OrdemServicoResponse
  fun atualizarStatus(id: Long, novoStatus: StatusOS, motivo: String?): OrdemServicoResponse
  fun consultar(id: Long): OrdemServicoResponse
  fun listarPorCliente(clienteId: Long): List<OrdemServicoResponse>
  fun cancelar(id: Long, motivo: String?)
}