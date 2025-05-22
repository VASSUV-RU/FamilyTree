CREATE TABLE IF NOT EXISTS persons (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    family_id BIGINT NOT NULL,
    user_id BIGINT,
    birth_date DATE,
    death_date DATE,
    gender VARCHAR(10),
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);