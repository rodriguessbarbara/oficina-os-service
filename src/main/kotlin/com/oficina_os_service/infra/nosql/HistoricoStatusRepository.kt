package com.oficina_os_service.infra.nosql

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface HistoricoStatusRepository : MongoRepository<HistoricoStatusDocument, String> {
  fun findAllByOsIdOrderByDataAlteracaoDesc(osId: Long): List<HistoricoStatusDocument>
}