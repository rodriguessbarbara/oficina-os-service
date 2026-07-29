# Arquitetura de microsserviços

Os diagramas abaixo foram renderizados em **28/07/2026** a partir do arquivo
[`diagrama-arquitetura.html`](diagrama-arquitetura.html).

## Visão geral dos componentes e da infraestrutura

![Visão geral da arquitetura](evidencias/diagrama-arquitetura.png)

## Saga coreografada

![Fluxo de eventos da Saga coreografada](evidencias/saga-coreografada.png)

## Sequência completa de uma Ordem de Serviço

![Happy path da Ordem de Serviço](evidencias/sequencia-happy-path.png)

## Rollback da Saga

![Fluxo de compensação da Saga](evidencias/saga-rollback.png)

## Isolamento dos bancos de dados

![Um banco por microsserviço](evidencias/isolamento-dados.png)
