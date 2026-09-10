ALTER TABLE faturamentos
    ALTER COLUMN clienteid DROP DEFAULT,
    ALTER COLUMN clienteid TYPE VARCHAR(120) USING clienteid::VARCHAR,
    ALTER COLUMN procedimento TYPE VARCHAR(160) USING procedimento::VARCHAR,
    ALTER COLUMN procedimento SET NOT NULL,
    ALTER COLUMN valorbrutofaturamento TYPE NUMERIC(12, 2) USING valorbrutofaturamento::NUMERIC(12, 2),
    ALTER COLUMN valorbrutofaturamento SET NOT NULL,
    ALTER COLUMN meiodepagamento TYPE VARCHAR(30),
    ALTER COLUMN meiodepagamento SET NOT NULL;

ALTER TABLE faturamentos RENAME COLUMN clienteid TO clientename;
ALTER TABLE faturamentos RENAME COLUMN meiodepagamento TO meiopagamento;
