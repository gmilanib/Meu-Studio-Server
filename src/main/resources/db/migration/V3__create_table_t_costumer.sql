CREATE TABLE IF NOT EXISTS t_costumer (
    id BIGSERIAL PRIMARY KEY,
    cpf VARCHAR(20),
    name VARCHAR(120),
    birthdate DATE,
    cellnumber VARCHAR(30),
    adress INTEGER,
    origin INTEGER
);
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY,
                                     username VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(256) not null,
    email VARCHAR(160) NOT NULL UNIQUE,
    telefone VARCHAR(20) UNIQUE ,
    Role varchar(6),
    criado_em TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP NOT NULL

    );