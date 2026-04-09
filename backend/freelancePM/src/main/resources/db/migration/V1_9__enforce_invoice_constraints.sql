-- Enforce NOT NULL constraints on numbering fields in invoice table
-- Note: Make sure existing data is already populated before running this.
ALTER TABLE invoice ALTER COLUMN year SET NOT NULL;
ALTER TABLE invoice ALTER COLUMN invoice_number SET NOT NULL;
ALTER TABLE invoice ALTER COLUMN sequence_number SET NOT NULL;

-- Ensure named unique constraints for future management
-- Drop and recreate the indices if needed to ensure they are named correctly as constraints
DROP INDEX IF EXISTS idx_invoice_number_unique;
ALTER TABLE invoice ADD CONSTRAINT unique_invoice_number UNIQUE (invoice_number);

DROP INDEX IF EXISTS idx_client_year_seq_unique;
ALTER TABLE invoice ADD CONSTRAINT unique_client_year_seq UNIQUE (client_id, year, sequence_number);
