CREATE TABLE user_roles (
    user_id     UUID            NOT NULL,
    roles       VARCHAR(50)     NOT NULL,

    CONSTRAINT pk_user_roles
        PRIMARY KEY (user_id, roles),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);