-- Migration: Create shifts table
-- For Coordinator to create pickup shifts for campaigns and stores

CREATE TABLE shifts (
    id_shift SERIAL PRIMARY KEY,
    id_campaign INT NOT NULL REFERENCES campaigns(id_campaign) ON DELETE CASCADE,
    id_store INT NOT NULL REFERENCES stores(id_store) ON DELETE CASCADE,
    shift_day DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    volunteers_needed INT NOT NULL CHECK (volunteers_needed > 0),
    location VARCHAR(500),
    observations VARCHAR(1000),
    created_by INT NOT NULL REFERENCES user_accounts(id_user),
    
    -- Constraint: end time must be after start time
    CHECK (end_time > start_time)
);

-- Index for faster queries
CREATE INDEX idx_shifts_campaign_store ON shifts(id_campaign, id_store);
CREATE INDEX idx_shifts_day ON shifts(shift_day);