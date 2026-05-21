CREATE TABLE captain_requests (
    id_request     SERIAL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    id_campaign    INT REFERENCES campaigns(id_campaign) ON DELETE CASCADE,
    id_coordinator INT REFERENCES user_accounts(id_user) ON DELETE SET NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    created_at     TIMESTAMP DEFAULT NOW(),
    resolved_at    TIMESTAMP,
    CHECK (status IN ('PENDIENTE', 'APROBADA', 'RECHAZADA'))
);

CREATE INDEX IF NOT EXISTS idx_captain_requests_status ON captain_requests(status);
CREATE INDEX IF NOT EXISTS idx_captain_requests_campaign ON captain_requests(id_campaign);
