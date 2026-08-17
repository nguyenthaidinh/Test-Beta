-- Permanent leaderboard for the total number of Gold Bars spent by each player.
-- One spent Gold Bar equals one point. This table is never reset by event resets.

CREATE TABLE IF NOT EXISTS `gold_bar_spend_top` (
    `player_id` BIGINT NOT NULL,
    `point` DECIMAL(65,0) UNSIGNED NOT NULL DEFAULT 0,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`player_id`),
    INDEX `idx_gold_bar_spend_top_point` (`point`, `player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Upgrade an existing BIGINT version without losing any accumulated points.
ALTER TABLE `gold_bar_spend_top`
    MODIFY COLUMN `point` DECIMAL(65,0) UNSIGNED NOT NULL DEFAULT 0;
