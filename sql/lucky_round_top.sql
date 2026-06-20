-- Lucky round leaderboard.
-- Run this on the game database before enabling the leaderboard.
-- Do not add this point column to `player`; that table can exceed MySQL row-size limits.

CREATE TABLE IF NOT EXISTS `lucky_round_top` (
    `player_id` BIGINT NOT NULL,
    `point` DECIMAL(65,0) UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (`player_id`),
    INDEX `idx_lucky_round_top_point` (`point`, `player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
