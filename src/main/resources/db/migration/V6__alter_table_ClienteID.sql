ALTER TABLE faturamentos

ALTER COLUMN clienteid TYPE STRING(255);

ALTER TABLE faturamentos RENAME COLUMN clienteid TO clientename;
