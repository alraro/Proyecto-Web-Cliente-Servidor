-- 1. CAMPAIGN TYPE
CREATE TABLE campaign_types (
    id_type   SERIAL PRIMARY KEY,
    name    VARCHAR(50) NOT NULL UNIQUE
);

-- 2. CAMPAIGN
CREATE TABLE campaigns (
    id_campaign   SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    id_type      INT REFERENCES campaign_types(id_type) ON DELETE SET NULL,
    start_date DATE NOT NULL,
    end_date    DATE NOT NULL,
    CHECK (end_date >= start_date)
);

-- 3. GEOGRAPHY
CREATE TABLE geographic_zones (
    id_zone  SERIAL PRIMARY KEY,
    name   VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE localities (
    id_locality SERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    id_zone      INT REFERENCES geographic_zones(id_zone) ON DELETE SET NULL
);

CREATE TABLE districts (
    id_district  SERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    id_locality INT REFERENCES localities(id_locality) ON DELETE CASCADE
);

CREATE TABLE postal_codes (
    postal_code           VARCHAR(10) PRIMARY KEY,
    id_locality INT NOT NULL REFERENCES localities(id_locality) ON DELETE CASCADE,
    id_district  INT REFERENCES districts(id_district) ON DELETE SET NULL
);

-- 4. CHAIN
CREATE TABLE chains (
    id_chain SERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    code    VARCHAR(50)  UNIQUE NOT NULL
);

-- 5. USER ACCOUNT
CREATE TABLE user_accounts (
    id_user  SERIAL PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    email       VARCHAR(255) UNIQUE NOT NULL,
    phone    VARCHAR(20),
    password  VARCHAR(255) NOT NULL,
    address   TEXT,
    postal_code          VARCHAR(10) REFERENCES postal_codes(postal_code) ON DELETE SET NULL
);

-- 6. PARTNER ENTITY
CREATE TABLE partner_entities (
    id_partner_entity SERIAL PRIMARY KEY,
    name                  VARCHAR(255) NOT NULL,
    address               TEXT,
    phone                VARCHAR(20)
);

-- 7. VOLUNTEER (independent from user_account)
CREATE TABLE volunteers (
    id_volunteer           SERIAL PRIMARY KEY,
    name                  VARCHAR(255) NOT NULL,
    phone                VARCHAR(20),
    email                   VARCHAR(255),
    address               TEXT,
    id_partner_entity INT REFERENCES partner_entities(id_partner_entity) ON DELETE SET NULL
);

-- 8. STORE
CREATE TABLE stores (
    id_store       SERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    address       TEXT,
    postal_code              VARCHAR(10) REFERENCES postal_codes(postal_code) ON DELETE SET NULL,
    id_chain       INT REFERENCES chains(id_chain) ON DELETE SET NULL,
    id_responsible  INT REFERENCES user_accounts(id_user) ON DELETE SET NULL
);

-- 9. STORES IN CAMPAIGN
CREATE TABLE campaign_stores (
    id_campaign INT REFERENCES campaigns(id_campaign) ON DELETE CASCADE,
    id_store  INT REFERENCES stores(id_store)   ON DELETE CASCADE,
    PRIMARY KEY (id_campaign, id_store)
);

-- 10. USER SUBCLASSES
CREATE TABLE administrators (
    id_user INT PRIMARY KEY REFERENCES user_accounts(id_user) ON DELETE CASCADE
);

CREATE TABLE partner_entity_managers (
    id_user              INT PRIMARY KEY REFERENCES user_accounts(id_user) ON DELETE CASCADE,
    id_partner_entity INT REFERENCES partner_entities(id_partner_entity) ON DELETE CASCADE
);

CREATE TABLE coordinators (
    id_user INT REFERENCES user_accounts(id_user) ON DELETE CASCADE,
    id_campaign INT REFERENCES campaigns(id_campaign) ON DELETE CASCADE,
    PRIMARY KEY (id_user, id_campaign)
);

CREATE TABLE captains (
    id_user INT REFERENCES user_accounts(id_user) ON DELETE CASCADE,
    id_campaign INT REFERENCES campaigns(id_campaign) ON DELETE CASCADE,
    PRIMARY KEY (id_user, id_campaign)
);

-- 11. VOLUNTEER_SHIFT
CREATE TABLE volunteer_shifts (
    id_volunteer INT,
    id_campaign    INT,
    id_store     INT,
    shift_day     DATE NOT NULL,
    start_time   TIME NOT NULL,
    end_time      TIME NOT NULL,
    attendance    BOOLEAN DEFAULT FALSE,
    notes TEXT,

    PRIMARY KEY (id_volunteer, id_campaign, id_store, shift_day, start_time),

    -- FKs
    FOREIGN KEY (id_volunteer)
        REFERENCES volunteers(id_volunteer)
        ON DELETE CASCADE,

    FOREIGN KEY (id_campaign, id_store)
        REFERENCES campaign_stores(id_campaign, id_store)
        ON DELETE CASCADE,

    -- Logical constraint
    CHECK (end_time > start_time)
);