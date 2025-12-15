-- Migration script to update exam_history table for new online exam workflow
-- Run this script manually in your MySQL database

USE quiz_web;

-- Add time_spent column if it doesn't exist
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'quiz_web' 
  AND TABLE_NAME = 'exam_history' 
  AND COLUMN_NAME = 'time_spent';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE exam_history ADD COLUMN time_spent BIGINT NULL COMMENT "Time spent in seconds"',
    'SELECT "time_spent already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify the changes
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'quiz_web' 
  AND TABLE_NAME = 'exam_history'
  AND COLUMN_NAME = 'time_spent';

SELECT 'Exam history migration completed successfully!' AS status;
