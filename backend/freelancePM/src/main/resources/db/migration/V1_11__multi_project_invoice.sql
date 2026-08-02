-- ============================================================
-- V1_11 — Multi-project invoice support
--
-- 1. Create the invoice_project join table
-- 2. Migrate all existing single project_id values into it
-- 3. Drop the now-redundant project_id column from invoice
-- ============================================================

-- 1. Join table
CREATE TABLE IF NOT EXISTS invoice_project (
    invoice_id INTEGER NOT NULL
        REFERENCES invoice(id) ON DELETE CASCADE,
    project_id INTEGER NOT NULL
        REFERENCES project(id) ON DELETE CASCADE,
    PRIMARY KEY (invoice_id, project_id)
);

-- 2. Migrate existing data (no data loss)
INSERT INTO invoice_project (invoice_id, project_id)
SELECT id, project_id
FROM   invoice
WHERE  project_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- 3. Remove the old single-project foreign key column
ALTER TABLE invoice DROP COLUMN IF EXISTS project_id;
