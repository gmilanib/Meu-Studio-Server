CREATE TABLE IF NOT EXISTS t_costumer (
    id BIGSERIAL PRIMARY KEY,
    cpf VARCHAR(20),
    name VARCHAR(120),
    birthdate DATE,
    cellnumber VARCHAR(30),
    adress INTEGER,
    origin INTEGER
);
