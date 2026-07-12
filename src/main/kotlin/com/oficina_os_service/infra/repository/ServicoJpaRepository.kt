package com.oficina_os_service.infra.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServicoJpaRepository : JpaRepository<ServicoEntity, Long>