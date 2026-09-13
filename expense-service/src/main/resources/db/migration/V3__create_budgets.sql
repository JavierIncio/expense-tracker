CREATE TABLE budgets
(
    id              UUID            PRIMARY KEY,
    user_id         UUID            NOT NULL,
    category_id     UUID            NOT NULL,
    year            INTEGER         NOT NULL CHECK (year >= 2000 AND year <= 2100),
    month           INTEGER         NOT NULL CHECK (month >= 1 AND month <= 12),
    amount          NUMERIC(12, 2)  NOT NULL,

    CONSTRAINT fk_budget_category
        FOREIGN KEY (category_id)
            REFERENCES categories (id),

    CONSTRAINT unique_budget_per_month
        UNIQUE (user_id, category_id, year, month)
);