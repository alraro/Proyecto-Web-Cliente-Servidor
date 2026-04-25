-- Migration: add participation field to chain table

ALTER TABLE chains
    ADD COLUMN participation BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN chains.participation IS 'Indicates whether the chain actively participates in campaigns';