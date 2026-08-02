-- Add timestamps to invoice table
ALTER TABLE invoice
ADD COLUMN sent_timestamp TIMESTAMP,
ADD COLUMN paid_timestamp TIMESTAMP;

-- Migrate existing last_sent_at data
UPDATE invoice SET sent_timestamp = last_sent_at WHERE last_sent_at IS NOT NULL;

-- Create invoice_status_audit_log table
CREATE TABLE invoice_status_audit_log (
    id BIGSERIAL PRIMARY KEY,
    invoice_id INTEGER NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(255),
    logged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create payment table
CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    invoice_id INTEGER NOT NULL REFERENCES invoice(id) ON DELETE CASCADE,
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    payment_method VARCHAR(50),
    reference_number VARCHAR(100),
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    recorded_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
