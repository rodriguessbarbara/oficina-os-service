package com.oficina_os_service.infra.controller

import com.oficina_os_service.application.OrdemServicoService
import com.oficina_os_service.infra.dto.AtualizarStatusRequest
import com.oficina_os_service.infra.dto.CriarOrdemRequest
import com.oficina_os_service.infra.dto.OrdemServicoResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/ordens")
@Tag(name = "Ordens de Serviço", description = "Gerenciamento do ciclo de vida das OS")
class OrdemServicoController(private val service: OrdemServicoService) {
  
  @PostMapping
  @Operation(summary = "Abre uma nova Ordem de Serviço")
  fun criar(@Valid @RequestBody request: CriarOrdemRequest): ResponseEntity<OrdemServicoResponse> =
    ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request))
  
  @PatchMapping("/{id}/status")
  @Operation(summary = "Atualiza o status de uma OS (uso manual / outros serviços via REST)")
  fun atualizarStatus(
    @PathVariable id: Long,
    @Valid @RequestBody request: AtualizarStatusRequest
  ): ResponseEntity<OrdemServicoResponse> =
    ResponseEntity.ok(service.atualizarStatus(id, request))
  
  @GetMapping("/{id}")
  @Operation(summary = "Consulta uma OS com histórico e resumo de orçamento/pagamento/execução")
  fun consultar(@PathVariable id: Long): ResponseEntity<OrdemServicoResponse> =
    ResponseEntity.ok(service.consultar(id))
  
  @GetMapping
  @Operation(summary = "Lista todas as OS de um cliente")
  fun listarPorCliente(@RequestParam clienteId: Long): ResponseEntity<List<OrdemServicoResponse>> =
    ResponseEntity.ok(service.listarPorCliente(clienteId))
  
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Cancela uma OS manualmente (transação compensatória)")
  fun cancelar(@PathVariable id: Long, @RequestParam(required = false) motivo: String?) {
    service.cancelar(id, motivo)
  }
}