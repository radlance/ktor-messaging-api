--liquibase formatted sql

--changeset dmitry:1

ALTER TABLE message
    ADD COLUMN type VARCHAR(10) DEFAULT 'normal' CHECK (type IN ('normal', 'system'));