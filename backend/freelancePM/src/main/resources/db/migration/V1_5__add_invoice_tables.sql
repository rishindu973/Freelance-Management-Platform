-- Altering existing invoice table from V1_1 to support new invoice generation features
ALTER TABLE invoice 
    ALTER COLUMN amount TYPE DECIMAL(15,2),
    ALTER COLUMN status SET DEFAULT 'DRAFT',
    ADD COLUMN IF NOT EXISTS subtotal DECIMAL(15,2),
    ADD COLUMN IF NOT EXISTS tax DECIMAL(15,2),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE IF NOT EXISTS invoice_line_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id INTEGER NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(15,2) NOT NULL CHECK (unit_price > 0),
    amount DECIMAL(15,2) NOT NULL
);
