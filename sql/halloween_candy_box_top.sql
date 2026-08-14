SET NAMES utf8mb4;

SET @schema_name := DATABASE();
SET @column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'player'
      AND COLUMN_NAME = 'point_halloween_candy_box'
);

SET @capsule_column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'player'
      AND COLUMN_NAME = 'point_halloween_capsule'
);

SET @after_column := IF(@capsule_column_exists > 0, 'point_halloween_capsule', 'point_halloween_box');

SET @add_column_sql := IF(
    @column_exists = 0,
    CONCAT('ALTER TABLE `player` ADD COLUMN `point_halloween_candy_box` int(11) NOT NULL DEFAULT 0 AFTER `', @after_column, '`'),
    'SELECT 1'
);

PREPARE stmt FROM @add_column_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
