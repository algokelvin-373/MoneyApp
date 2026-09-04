CREATE TABLE IF NOT EXISTS users (
    id              TEXT PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    email           VARCHAR(320) NOT NULL,
    password_hash   TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT users_email_not_blank CHECK (BTRIM(email) <> '')
);

CREATE UNIQUE INDEX IF NOT EXISTS users_email_unique_ci
    ON users (LOWER(email));

CREATE TABLE IF NOT EXISTS accounts (
    id          TEXT PRIMARY KEY,
    user_id     TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code        VARCHAR(40) NOT NULL,
    name        VARCHAR(120) NOT NULL,
    balance     BIGINT NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT accounts_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT accounts_user_code_unique UNIQUE (user_id, code),
    CONSTRAINT accounts_id_user_unique UNIQUE (id, user_id)
);

CREATE INDEX IF NOT EXISTS accounts_user_id_idx
    ON accounts (user_id, code);

CREATE TABLE IF NOT EXISTS transaction_codes (
    id          TEXT PRIMARY KEY,
    code        VARCHAR(40) NOT NULL UNIQUE,
    name        VARCHAR(120) NOT NULL,
    type        VARCHAR(16) NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT transaction_codes_type_valid CHECK (type IN ('INCOME', 'EXPENSE'))
);

CREATE TABLE IF NOT EXISTS transactions (
    id                    TEXT PRIMARY KEY,
    user_id               TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id            TEXT NOT NULL,
    transaction_code_id   TEXT NOT NULL REFERENCES transaction_codes(id),
    transaction_type      VARCHAR(16) NOT NULL,
    amount                BIGINT NOT NULL,
    transaction_date      TIMESTAMPTZ NOT NULL,
    note                  TEXT NOT NULL DEFAULT '',
    idempotency_key       VARCHAR(160) NOT NULL DEFAULT '',
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at            TIMESTAMPTZ,
    CONSTRAINT transactions_account_owner_fk
        FOREIGN KEY (account_id, user_id) REFERENCES accounts(id, user_id),
    CONSTRAINT transactions_type_valid CHECK (transaction_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT transactions_amount_positive CHECK (amount > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS transactions_idempotency_unique
    ON transactions (user_id, idempotency_key)
    WHERE idempotency_key <> '' AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS transactions_user_history_idx
    ON transactions (user_id, transaction_date DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS transactions_user_account_idx
    ON transactions (user_id, account_id, transaction_date DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS transactions_user_code_idx
    ON transactions (user_id, transaction_code_id, transaction_date DESC)
    WHERE deleted_at IS NULL;

INSERT INTO transaction_codes (id, code, name, type) VALUES
    ('code-food', 'FOOD', 'Makanan & Minuman', 'EXPENSE'),
    ('code-transport', 'TRANSPORT', 'Transportasi', 'EXPENSE'),
    ('code-shopping', 'SHOPPING', 'Belanja', 'EXPENSE'),
    ('code-bill', 'BILL', 'Tagihan', 'EXPENSE'),
    ('code-entertainment', 'ENTERTAINMENT', 'Hiburan', 'EXPENSE'),
    ('code-health', 'HEALTH', 'Kesehatan', 'EXPENSE'),
    ('code-education', 'EDUCATION', 'Pendidikan', 'EXPENSE'),
    ('code-social', 'SOCIAL', 'Sosial', 'EXPENSE'),
    ('code-other-expense', 'OTHER_EXPENSE', 'Pengeluaran Lainnya', 'EXPENSE'),
    ('code-salary', 'SALARY', 'Gaji', 'INCOME'),
    ('code-bonus', 'BONUS', 'Bonus', 'INCOME'),
    ('code-refund', 'REFUND', 'Refund', 'INCOME'),
    ('code-cashback', 'CASHBACK', 'Cashback', 'INCOME'),
    ('code-other-income', 'OTHER_INCOME', 'Pemasukan Lainnya', 'INCOME')
ON CONFLICT (code) DO NOTHING;
