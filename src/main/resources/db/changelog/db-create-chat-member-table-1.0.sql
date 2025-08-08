--liquibase formatted sql

--changeset dmitry:1

CREATE TABLE IF NOT EXISTS chat_member
(
    chat_id   INTEGER REFERENCES chat (id) ON DELETE CASCADE,
    user_id   INTEGER REFERENCES users (id) ON DELETE CASCADE,
    role      VARCHAR(6) DEFAULT 'member',
    joined_at TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (chat_id, user_id)
);