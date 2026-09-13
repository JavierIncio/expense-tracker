CREATE TABLE transactions
(
    id              UUID            PRIMARY KEY,
    user_id         UUID            NOT NULL,
    type            VARCHAR(50)     NOT NULL,
    amount          NUMERIC(12, 2)  NOT NULL,
    category_id     UUID            NOT NULL,
    description     TEXT,
    date            DATE            NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL,

    CONSTRAINT fk_transaction_category
        FOREIGN KEY (category_id)
            REFERENCES categories (id)
);