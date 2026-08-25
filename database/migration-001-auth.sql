BEGIN;
ALTER TABLE merchants ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
UPDATE merchants
SET password_hash = '$2a$10$7EqJtq98hPqEX7fNZaFWoO4s9R3G3X4wBq6oXG1Pq5s6fZf2XvYqK'
WHERE password_hash IS NULL;
ALTER TABLE merchants ALTER COLUMN password_hash SET NOT NULL;
COMMIT;
