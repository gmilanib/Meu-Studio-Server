ALTER TABLE faturamentos
    ADD COLUMN horariofaturamento TIME NOT NULL DEFAULT '00:00:00';

ALTER TABLE faturamentos
    ALTER COLUMN horariofaturamento DROP DEFAULT;
