CREATE TABLE categories (
    id          UUID            PRIMARY KEY,
    user_id     UUID            NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    type        VARCHAR(50)     NOT NULL,
    created_at  TIMESTAMPTZ     NOT NULL,
    updated_at  TIMESTAMPTZ     NOT NULL
);