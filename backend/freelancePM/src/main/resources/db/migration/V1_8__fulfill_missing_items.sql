-- V1_8__fulfill_missing_items.sql
-- This script fulfills missing database schema items to match the JPA Entity definitions

-- 1. Create activity table missing from previous migrations
CREATE TABLE IF NOT EXISTS activity (
    id BIGSERIAL PRIMARY KEY,
    manager_id INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    timestamp TIMESTAMP NOT NULL
);

-- 2. Add missing columns to users table
ALTER TABLE users 
    ADD COLUMN IF NOT EXISTS reset_password_token VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reset_password_expires TIMESTAMP;

-- 3. Add missing columns to client table
ALTER TABLE client 
    ADD COLUMN IF NOT EXISTS address VARCHAR(255);

-- 4. Add missing columns to invoice table to align with Invoice.java @Table(name = "invoice")
ALTER TABLE invoice
    ADD COLUMN IF NOT EXISTS subtotal DECIMAL(15,2),
    ADD COLUMN IF NOT EXISTS tax DECIMAL(15,2),
    ADD COLUMN IF NOT EXISTS invoice_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS sequence_number INTEGER,
    ADD COLUMN IF NOT EXISTS year INTEGER,
    ADD COLUMN IF NOT EXISTS due_date DATE,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- 5. Create invoice_line_items table (previously in deleted V1_5)
CREATE TABLE IF NOT EXISTS invoice_line_items (
    id SERIAL PRIMARY KEY,
    invoice_id INTEGER NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    amount DECIMAL(15,2) NOT NULL
);
