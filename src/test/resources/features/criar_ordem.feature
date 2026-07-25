Feature: Criação de Ordem de Serviço – Happy Path da Saga

  Scenario: Abertura de OS com dados válidos
    Given um cliente com id 1 e veiculo com id 10
    When uma OS e criada para o cliente 1 e veiculo 10
    Then a OS deve ser criada com status "RECEBIDA"
    And a resposta deve ter status HTTP 201

  Scenario: Tentativa de criar OS sem clienteId
    When uma OS e criada para o cliente 0 e veiculo 10
    Then a resposta deve ter status HTTP 400
