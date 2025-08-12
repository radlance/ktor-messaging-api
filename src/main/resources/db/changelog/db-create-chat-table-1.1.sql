--liquibase formatted sql

--changeset dmitry:1

ALTER TABLE chat
    ADD CONSTRAINT type_name CHECK (type IN ('group', 'private'))