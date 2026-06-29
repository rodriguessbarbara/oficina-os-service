-- Tabela principal de OS
CREATE TABLE tb_ordem_servico (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    veiculo_id BIGINT NOT NULL,
    orcamento_id BIGINT,
    status VARCHAR(50) NOT NULL,
    data_abertura TIMESTAMP NOT NULL,
    data_encerramento TIMESTAMP,
    inicio_execucao TIMESTAMP,
    fim_execucao TIMESTAMP
);

-- Tabela de Itens de Serviço
CREATE TABLE tb_item_servico (
    id BIGSERIAL PRIMARY KEY,
    servico_id BIGINT NOT NULL,
    ordem_servico_id BIGINT NOT NULL,
    preco_aplicado NUMERIC(19, 2) NOT NULL,
    quantidade INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    inicio_execucao TIMESTAMP,
    fim_execucao TIMESTAMP,
    CONSTRAINT fk_item_servico_os FOREIGN KEY (ordem_servico_id) REFERENCES tb_ordem_servico(id)
);

-- Tabela de itens do estoque
CREATE TABLE tb_item_estoque (
    id BIGSERIAL PRIMARY KEY,
    estoque_id BIGINT NOT NULL,
    ordem_servico_id BIGINT NOT NULL,
    quantidade NUMERIC(19, 2) NOT NULL,
    preco_unitario NUMERIC(19, 2) NOT NULL,
    CONSTRAINT fk_item_estoque_os FOREIGN KEY (ordem_servico_id) REFERENCES tb_ordem_servico(id)
);

CREATE INDEX idx_os_cliente ON tb_ordem_servico(cliente_id);
CREATE INDEX idx_os_status ON tb_ordem_servico(status);
CREATE INDEX idx_item_servico_os ON tb_item_servico(ordem_servico_id);
CREATE INDEX idx_item_estoque_os ON tb_item_estoque(ordem_servico_id);