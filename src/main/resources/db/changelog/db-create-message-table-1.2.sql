--liquibase formatted sql

--changeset dmitry:1

ALTER TABLE message
    ALTER COLUMN sender_id DROP NOT NULL;