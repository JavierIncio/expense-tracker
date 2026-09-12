CREATE TABLE users (
    id              UUID            PRIMARY KEY,
    username        VARCHAR(30)     NOT NULL UNIQUE,
    email           VARCHAR(100)    NOT NULL UNIQUE,
    password_hash   VARCHAR(60)     NOT NULL,
    first_name      VARCHAR(50),
    last_name       VARCHAR(50),
    enabled         BOOLEAN         NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,
    updated_at      TIMESTAMPTZ     NOT NULL
);