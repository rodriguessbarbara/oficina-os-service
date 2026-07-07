CREATE TABLE tb_servico (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco NUMERIC(19, 2) NOT NULL
);

CREATE INDEX idx_servico_nome ON tb_servico(nome);

ALTER TABLE tb_item_servico
ADD CONSTRAINT fk_item_servico_catalogo
FOREIGN KEY (servico_id) REFERENCES tb_servico(id);