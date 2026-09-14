ALTER TABLE faturamentos
    ADD COLUMN cliente_id BIGINT REFERENCES clientes(id);

UPDATE faturamentos faturamento
SET cliente_id = candidato.id
FROM (
    SELECT MIN(id) AS id, LOWER(TRIM(nome)) AS nome_normalizado
    FROM clientes
    GROUP BY LOWER(TRIM(nome))
    HAVING COUNT(*) = 1
) candidato
WHERE LOWER(TRIM(faturamento.clientename)) = candidato.nome_normalizado;

CREATE INDEX idx_faturamentos_cliente_id ON faturamentos(cliente_id);
