-- See specs/001-backend-para-frontend/data-model.md §2 for the full rationale behind each
-- constraint and index below (composite FK, case-insensitive platform uniqueness, etc).

CREATE TABLE platforms (
    user_id    UUID        NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name       TEXT        NOT NULL,
    type       TEXT        NOT NULL DEFAULT 'Other',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, name),
    CONSTRAINT platforms_name_len CHECK (char_length(btrim(name)) BETWEEN 1 AND 120),
    CONSTRAINT platforms_type_len CHECK (char_length(btrim(type)) BETWEEN 1 AND 40)
);

-- Case-insensitive uniqueness: 'Binance' and 'binance' can't coexist for the same user.
CREATE UNIQUE INDEX platforms_user_lower_name_uk ON platforms (user_id, lower(name));

CREATE TABLE holdings (
    id            UUID          PRIMARY KEY,
    user_id       UUID          NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name          TEXT          NOT NULL,
    asset_class   TEXT          NOT NULL,
    platform_name TEXT          NOT NULL,
    value_usd     NUMERIC(20,2) NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT holdings_value_non_negative CHECK (value_usd >= 0),
    CONSTRAINT holdings_name_len  CHECK (char_length(btrim(name))        BETWEEN 1 AND 120),
    CONSTRAINT holdings_class_len CHECK (char_length(btrim(asset_class)) BETWEEN 1 AND 60),
    -- A holding can only reference a platform belonging to the SAME user.
    CONSTRAINT holdings_platform_fk FOREIGN KEY (user_id, platform_name)
        REFERENCES platforms (user_id, name) ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX holdings_user_created_idx  ON holdings (user_id, created_at);
CREATE INDEX holdings_user_class_idx    ON holdings (user_id, asset_class);
CREATE INDEX holdings_user_platform_idx ON holdings (user_id, platform_name);

CREATE TABLE net_worth_snapshots (
    id              UUID          PRIMARY KEY,
    user_id         UUID          NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    captured_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    total_value_usd NUMERIC(20,2) NOT NULL,
    CONSTRAINT snapshots_value_non_negative CHECK (total_value_usd >= 0),
    -- One snapshot per second per user — protects against a double-click (CA-06.4).
    CONSTRAINT snapshots_user_instant_uk UNIQUE (user_id, captured_at)
);

CREATE INDEX snapshots_user_captured_idx ON net_worth_snapshots (user_id, captured_at);

ALTER TABLE platforms           ENABLE ROW LEVEL SECURITY;
ALTER TABLE holdings            ENABLE ROW LEVEL SECURITY;
ALTER TABLE net_worth_snapshots ENABLE ROW LEVEL SECURITY;

-- Defense in depth (D5): the backend connects with a privileged role and isn't limited by
-- these, but they keep PostgREST/the anon key locked out if that route is ever exposed.
CREATE POLICY "own platforms" ON platforms
    FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "own holdings" ON holdings
    FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "own snapshots" ON net_worth_snapshots
    FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
