SET NAMES utf8mb4;

SET @schema_name := DATABASE();
SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'player'
      AND COLUMN_NAME = 'point_halloween_capsule'
);

SET @add_column_sql := IF(
    @column_exists = 0,
    'ALTER TABLE `player` ADD COLUMN `point_halloween_capsule` int(11) NOT NULL DEFAULT 0 AFTER `point_halloween_box`',
    'SELECT 1'
);

PREPARE stmt FROM @add_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
