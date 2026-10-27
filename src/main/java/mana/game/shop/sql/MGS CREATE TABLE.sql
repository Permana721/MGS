-- CREATE TABLE games
CREATE TYPE game_category AS ENUM (
    'ACTION',
    'RPG',
    'SPORTS',
    'STRATEGY',
    'ADVENTURE'
);

CREATE TYPE game_type AS ENUM (
    'DIGITAL',
    'PHYSICAL'
);

CREATE TABLE games (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(250) NOT NULL,
    category game_category NOT NULL,
    price NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (price >= 0),
    type game_type NOT NULL,
    stock INTEGER,
    size_mb INTEGER,
    release_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_game_stock CHECK (
        (type = 'PHYSICAL' AND stock IS NOT NULL AND stock >= 0) OR
        (type = 'DIGITAL' AND stock IS NULL)
    ),

    CONSTRAINT check_game_size CHECK (
        (type = 'DIGITAL' AND size_mb IS NOT NULL AND size_mb > 0) OR
        (type = 'PHYSICAL' AND size_mb IS NULL)
    )
);

CREATE INDEX idx_games_category ON games(category);
CREATE INDEX idx_games_title ON games(title);

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_games_title_trgm ON games USING gin (titlegin_trgm_ops);

COMMIT;
-- END TABLE games

-- CREATE TABLE users
CREATE TYPE user_role AS ENUM (
    'ADMIN',
    'CUSTOMER'
);

CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(250) NOT NULL UNIQUE,
    password VARCHAR(72) NOT NULL,
    email VARCHAR(250) NOT NULL UNIQUE,
    role user_role NOT NULL DEFAULT 'CUSTOMER',
    balance NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_email_valid CHECK (email LIKE '%@%.com')
);

COMMIT;
-- END TABLE users

-- CREATE TABLE transactions
CREATE TYPE transaction_status AS ENUM (
    'PENDING',
    'SUCCESS',
    'FAILED',
    'REFUNDED'
);

CREATE TABLE transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    game_id BIGINT NOT NULL,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    status transaction_status NOT NULL DEFAULT 'SUCCESS',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE RESTRICT,
        
    CONSTRAINT fk_transactions_game FOREIGN KEY (game_id) 
        REFERENCES games(id) ON DELETE RESTRICT
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_game_id ON transactions(game_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);

ALTER TABLE transactions
ADD COLUMN coupon_id INT REFERENCES coupons(id),
ADD COLUMN original_amount NUMERIC(12, 2),
ADD COLUMN discount_amount NUMERIC(12, 2) DEFAULT 0;

COMMIT;
-- END TABLE transactions

-- CREATE TABLE coupons
CREATE TABLE coupons (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_percent NUMERIC(5, 2) NOT NULL,
    max_discount NUMERIC(12, 2),
    min_purchase NUMERIC(12, 2) DEFAULT 0,
    usage_limit INT DEFAULT 1,
    used_count INT DEFAULT 0,
    valid_from TIMESTAMPTZ NOT NULL,
    valid_until TIMESTAMPTZ NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
-- END TABLE coupons

-- CREATE TABLE user_coupons
CREATE TABLE user_coupons (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    coupon_id INT NOT NULL REFERENCES coupons(id) ON DELETE CASCADE,
    used_at TIMESTAMPTZ,
    is_redeemed BOOLEAN DEFAULT FALSE,
    UNIQUE (user_id, coupon_id)
);
-- END TABLE user_coupons