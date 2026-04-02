-- Refined Invoice Numbering (Approach B)
-- Add new columns to invoices
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS year INTEGER;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS invoice_number VARCHAR(50);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS sequence_number INTEGER;

-- Create sequence tracking table
CREATE TABLE IF NOT EXISTS client_invoice_sequence (
    id BIGSERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL REFERENCES client(id) ON DELETE CASCADE,
    year INTEGER NOT NULL,
    current_sequence INTEGER NOT NULL DEFAULT 0,
    UNIQUE (client_id, year)
);

-- Add unique constraints
CREATE UNIQUE INDEX IF NOT EXISTS idx_invoice_number_unique ON invoices (invoice_number);
CREATE UNIQUE INDEX IF NOT EXISTS idx_client_year_seq_unique ON invoices (client_id, year, sequence_number);
