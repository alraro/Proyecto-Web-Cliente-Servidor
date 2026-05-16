CREATE TABLE IF NOT EXISTS incidents (
    id_incident  SERIAL PRIMARY KEY,
    id_campaign  INT REFERENCES campaigns(id_campaign) ON DELETE CASCADE,
    id_store     INT REFERENCES stores(id_store) ON DELETE CASCADE,
    id_user      INT REFERENCES user_accounts(id_user) ON DELETE SET NULL,
    description  TEXT NOT NULL,
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_incidents_campaign_store ON incidents(id_campaign, id_store);
CREATE INDEX IF NOT EXISTS idx_incidents_user ON incidents(id_user);
