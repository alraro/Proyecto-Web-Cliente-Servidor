-- Migration: captain-to-shift assignments
-- A captain (user_account) can be assigned to a specific shift

CREATE TABLE shift_captains (
    id_shift INT NOT NULL REFERENCES shifts(id_shift) ON DELETE CASCADE,
    id_user  INT NOT NULL REFERENCES user_accounts(id_user) ON DELETE CASCADE,
    PRIMARY KEY (id_shift, id_user)
);

CREATE INDEX idx_shift_captains_shift ON shift_captains(id_shift);
CREATE INDEX idx_shift_captains_user  ON shift_captains(id_user);
