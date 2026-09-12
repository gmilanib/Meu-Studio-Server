CREATE TABLE procedimentos (
    id UUID PRIMARY KEY,
    nome VARCHAR(160) NOT NULL CHECK (length(trim(nome)) > 0),
    descricao VARCHAR(2000),
    preco NUMERIC(12,2) NOT NULL CHECK (preco > 0),
    duracao_minutos INTEGER NOT NULL CHECK (duracao_minutos > 0),
    categoria VARCHAR(80),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX procedimentos_nome_unico ON procedimentos (lower(trim(nome)));

-- O nome e o valor históricos permanecem nos faturamentos anteriores.
ALTER TABLE faturamentos ADD COLUMN procedimento_id UUID REFERENCES procedimentos(id);
