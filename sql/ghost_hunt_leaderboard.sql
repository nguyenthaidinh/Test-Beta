-- BXH săn Hồn Ma độc lập với event_point và các BXH khác.
-- Chạy file này trên database online trước khi mở sự kiện.

CREATE TABLE IF NOT EXISTS `ghost_hunt_leaderboard` (
    `player_id` BIGINT NOT NULL,
    `point` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`player_id`),
    INDEX `idx_ghost_hunt_point` (`point`, `player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
