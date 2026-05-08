-- Adapta o catalogo de produtos para o dominio de floricultura.
-- Mantem a tabela produto para preservar o CRUD existente e evitar refatoracao ampla.

ALTER TABLE produto
    ADD COLUMN categoria VARCHAR(30) NOT NULL DEFAULT 'FLOR_CORTE',
    ADD COLUMN cor VARCHAR(60),
    ADD COLUMN quantidade_estoque INTEGER NOT NULL DEFAULT 0;

ALTER TABLE produto
    ADD CONSTRAINT chk_produto_categoria_floricultura
        CHECK (categoria IN ('FLOR_CORTE', 'ARRANJO', 'PLANTA', 'PRESENTE', 'ACESSORIO')),
    ADD CONSTRAINT chk_produto_quantidade_estoque
        CHECK (quantidade_estoque >= 0);

CREATE INDEX idx_produto_cor ON produto (cor);

COMMENT ON COLUMN produto.categoria IS 'Categoria do item da floricultura';
COMMENT ON COLUMN produto.cor IS 'Cor predominante do item, quando aplicavel';
COMMENT ON COLUMN produto.quantidade_estoque IS 'Quantidade disponivel em estoque';
