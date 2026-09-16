CREATE TABLE notifications (
    id              UUID            PRIMARY KEY,
    user_id         UUID            NOT NULL,
    message         VARCHAR(255)    NOT NULL,
    read            BOOLEAN         NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL
);