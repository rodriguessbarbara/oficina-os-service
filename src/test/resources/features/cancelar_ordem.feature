Feature: Cancelamento de OS – Rollback da Saga

  Scenario: Cancelamento manual de OS existente
    Given uma OS existente com id 1
    When o cancelamento da OS 1 e solicitado com motivo "Orçamento rejeitado pelo cliente"
    Then a OS deve ter status CANCELADA

  Scenario: Cancelamento de OS inexistente retorna 404
    When o cancelamento da OS 9999 e solicitado com motivo "Teste"
    Then o cancelamento deve retornar status HTTP 404