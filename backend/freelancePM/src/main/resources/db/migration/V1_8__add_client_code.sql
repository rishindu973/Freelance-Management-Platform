-- Add code column to client table
ALTER TABLE client ADD COLUMN IF NOT EXISTS code VARCHAR(10);

-- Populate with default if needed (e.g., first 3 chars of name, uppercase)
UPDATE client SET code = UPPER(LEFT(name, 3)) WHERE code IS NULL;
