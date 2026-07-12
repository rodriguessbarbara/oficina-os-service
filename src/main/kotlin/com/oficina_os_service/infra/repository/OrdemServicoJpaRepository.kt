package com.oficina_os_service.infra.repository

import com.oficina_os_service.domain.enum.StatusOS
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrdemServicoJpaRepository : JpaRepository<OrdemServicoEntity, Long> {
  fun findAllByClienteId(clienteId: Long): List<OrdemServicoEntity>
  fun findAllByStatus(status: StatusOS): List<OrdemServicoEntity>
}