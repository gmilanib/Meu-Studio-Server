CREATE TABLE IF NOT EXISTS faturamentos(
    fatID UUID PRIMARY KEY,
    dataFaturamento DATE not null,
    clienteID BIGSERIAL,
    procedimento UUID,
    valorBrutofaturamento FLOAT.meioDePagamento char(12)
);