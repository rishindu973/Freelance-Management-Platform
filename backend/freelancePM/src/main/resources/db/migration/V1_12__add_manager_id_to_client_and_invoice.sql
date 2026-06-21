ALTER TABLE client ADD COLUMN manager_id INT;
UPDATE client SET manager_id = 269 WHERE manager_id IS NULL;

ALTER TABLE invoice ADD COLUMN manager_id INT;
UPDATE invoice SET manager_id = 269 WHERE manager_id IS NULL;

