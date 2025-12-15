-- PART 1B: Add new columns to exam_online
-- Run this if you got error on DROP COLUMN submission_deadline

USE quiz_web;

-- Add duration_minutes
ALTER TABLE exam_online 
    ADD COLUMN duration_minutes INT NOT NULL DEFAULT 60
    COMMENT 'Exam duration in minutes';

-- Add started_at  
ALTER TABLE exam_online 
    ADD COLUMN started_at DATETIME NULL
    COMMENT 'When teacher actually started the exam';

-- Add finished_at
ALTER TABLE exam_online 
    ADD COLUMN finished_at DATETIME NULL
    COMMENT 'When teacher ended the exam';

-- PART 2: Add time_spent to exam_history
ALTER TABLE exam_history 
    ADD COLUMN time_spent BIGINT NULL
    COMMENT 'Time student took to complete exam in seconds';

-- Verify
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'quiz_web' 
  AND TABLE_NAME = 'exam_online'
  AND COLUMN_NAME IN ('duration_minutes', 'started_at', 'finished_at')
ORDER BY COLUMN_NAME;

SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'quiz_web' 
  AND TABLE_NAME = 'exam_history'
  AND COLUMN_NAME = 'time_spent';

SELECT '✅ Migration completed!' AS status;
