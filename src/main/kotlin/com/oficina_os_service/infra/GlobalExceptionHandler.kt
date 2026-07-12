package com.oficina_os_service.infra

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {
  
  @ExceptionHandler(EntityNotFoundException::class)
  fun handleNotFound(ex: EntityNotFoundException): ProblemDetail =
    ProblemDetail.forStatus(HttpStatus.NOT_FOUND).also {
      it.title = "Recurso não encontrado"
      it.detail = ex.message
      it.setProperty("timestamp", Instant.now())
    }
  
  @ExceptionHandler(IllegalStateException::class)
  fun handleIllegalState(ex: IllegalStateException): ProblemDetail =
    ProblemDetail.forStatus(HttpStatus.CONFLICT).also {
      it.title = "Operação inválida"
      it.detail = ex.message
      it.setProperty("timestamp", Instant.now())
    }
  
  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidation(ex: MethodArgumentNotValidException): ProblemDetail =
    ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).also {
      it.title = "Dados inválidos"
      val erros = ex.bindingResult.allErrors.associate { error ->
        val field = if (error is FieldError) error.field else error.objectName
        field to (error.defaultMessage ?: "inválido")
      }
      it.setProperty("erros", erros)
      it.setProperty("timestamp", Instant.now())
    }
  
  @ExceptionHandler(Exception::class)
  fun handleGeneric(ex: Exception): ProblemDetail =
    ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR).also {
      it.title = "Erro interno"
      it.detail = ex.message
      it.setProperty("timestamp", Instant.now())
    }
}