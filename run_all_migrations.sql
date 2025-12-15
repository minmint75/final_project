-- Complete database migration for Online Exam Feature Phase 1 & 2
-- Run this script in MySQL Workbench or command line
-- Database: quiz_web
-- Compatible with MySQL 5.7+

USE quiz_web;

-- =============================================================================
-- PART 1: Update exam_online table
-- =============================================================================

SELECT '=== Updating exam_online table ===' AS status;

-- Drop old columns (without IF EXISTS - will error if column doesn't exist, that's ok)
-- If you get error "Can't DROP column", that means it's already dropped - ignore it
ALTER TABLE exam_online DROP COLUMN num_of_questions;
ALTER TABLE exam_online DROP COLUMN submission_deadline;

-- Add new columns
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

SELECT 'exam_online table updated' AS status;

-- =============================================================================
-- PART 2: Update exam_history table
-- =============================================================================

SELECT '=== Updating exam_history table ===' AS status;

-- Add time_spent
ALTER TABLE exam_history 
    ADD COLUMN time_spent BIGINT NULL
    COMMENT 'Time student took to complete exam in seconds';

SELECT 'exam_history table updated' AS status;

-- =============================================================================
-- VERIFICATION
-- =============================================================================

SELECT '=== Verification ===' AS status;

-- Show exam_online structure
SELECT 'exam_online columns:' AS info;
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'quiz_web' 
  AND TABLE_NAME = 'exam_online'
ORDER BY ORDINAL_POSITION;

-- Show exam_history structure  
SELECT 'exam_history columns:' AS info;
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'quiz_web' 
  AND TABLE_NAME = 'exam_history'
ORDER BY ORDINAL_POSITION;

SELECT '✅ Migration completed successfully!' AS final_status;
