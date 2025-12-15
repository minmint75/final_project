-- Migration script to update exam_online table for new online exam workflow
-- Run this script manually in your MySQL database

USE quiz_web;

-- Step 1: Drop old columns that are no longer in the entity
ALTER TABLE exam_online 
    DROP COLUMN IF EXISTS num_of_questions,
    DROP COLUMN IF EXISTS submission_deadline;

-- Step 2: Add new columns that exist in entity but not in database
-- (These might already exist if Hibernate created them, but we add IF NOT EXISTS for safety)

-- Add duration_minutes if it doesn't exist
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'quiz_web' 
  AND TABLE_NAME = 'exam_online' 
  AND COLUMN_NAME = 'duration_minutes';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE exam_online ADD COLUMN duration_minutes INT NOT NULL DEFAULT 60',
    'SELECT "duration_minutes already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add started_at if it doesn't exist
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'quiz_web' 
  AND TABLE_NAME = 'exam_online' 
  AND COLUMN_NAME = 'started_at';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE exam_online ADD COLUMN started_at DATETIME NULL',
    'SELECT "started_at already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add finished_at if it doesn't exist
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'quiz_web' 
  AND TABLE_NAME = 'exam_online' 
  AND COLUMN_NAME = 'finished_at';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE exam_online ADD COLUMN finished_at DATETIME NULL',
    'SELECT "finished_at already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 3: Verify the changes
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'quiz_web' 
  AND TABLE_NAME = 'exam_online'
ORDER BY ORDINAL_POSITION;

-- Step 4: Add WAITING status to any existing enums if needed
-- (This is handled by JPA automatically for enum types)

SELECT 'Migration completed successfully!' AS status;
