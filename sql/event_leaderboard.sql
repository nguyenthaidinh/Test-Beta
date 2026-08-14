-- Event leaderboard storage.
-- Keeps race/top points outside `player` so normal player saves cannot reset them.

CREATE TABLE IF NOT EXISTS `event_leaderboard` (
    `event_key` VARCHAR(64) NOT NULL,
    `player_id` BIGINT NOT NULL,
    `point` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`event_key`, `player_id`),
    INDEX `idx_event_leaderboard_top` (`event_key`, `point`, `player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Migrate current Halloween points from player columns.
-- Safe to run many times: it never lowers existing event_leaderboard points.

INSERT INTO `event_leaderboard` (`event_key`, `player_id`, `point`)
SELECT 'halloween_box', `id`, `point_halloween_box`
FROM `player`
WHERE `point_halloween_box` > 0
ON DUPLICATE KEY UPDATE `point` = GREATEST(`point`, VALUES(`point`));

INSERT INTO `event_leaderboard` (`event_key`, `player_id`, `point`)
SELECT 'halloween_capsule', `id`, `point_halloween_capsule`
FROM `player`
WHERE `point_halloween_capsule` > 0
ON DUPLICATE KEY UPDATE `point` = GREATEST(`point`, VALUES(`point`));

INSERT INTO `event_leaderboard` (`event_key`, `player_id`, `point`)
SELECT 'halloween_candy_box', `id`, `point_halloween_candy_box`
FROM `player`
WHERE `point_halloween_candy_box` > 0
ON DUPLICATE KEY UPDATE `point` = GREATEST(`point`, VALUES(`point`));
