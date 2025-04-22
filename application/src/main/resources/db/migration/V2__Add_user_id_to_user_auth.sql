-- V2__Add_user_id_to_user_auth.sql

-- 1. Add the column, allowing NULLs initially
ALTER TABLE user_auth ADD COLUMN user_id VARCHAR(255);

-- 2. Populate the new column for existing rows.
--    You MUST decide what value to put here. Examples:
--    - A default placeholder:
--      UPDATE user_auth SET user_id = 'UNKNOWN_OR_DEFAULT' WHERE user_id IS NULL;
--    - Generate based on existing data (if possible):
--      UPDATE user_auth SET user_id = 'user_' || id WHERE user_id IS NULL; -- If you have an 'id' column
--    - !! This step is CRITICAL and depends on your application logic !!
--    !! Ensure ALL existing rows get a non-null value !!
UPDATE user_auth SET user_id = gen_random_uuid()::varchar WHERE user_id IS NULL; -- Example using PostgreSQL function if appropriate

-- 3. Now that all rows have a value, apply the NOT NULL constraint
ALTER TABLE user_auth ALTER COLUMN user_id SET NOT NULL;

-- 4. Optional: Add foreign key constraints, indexes etc. if needed
-- ALTER TABLE user_auth ADD CONSTRAINT fk_user_auth_user_id FOREIGN KEY (user_id) REFERENCES users(id);
-- CREATE INDEX idx_user_auth_user_id ON user_auth (user_id);
