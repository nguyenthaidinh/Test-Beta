-- Migration production cho sự kiện Mưa Máu, Hồn Ma và Sói Địa Ngục.
-- Chạy một lần trên database online trước khi khởi động bản server mới.

SET NAMES utf8mb4;
START TRANSACTION;

CREATE TABLE IF NOT EXISTS `ghost_hunt_leaderboard` (
    `player_id` BIGINT NOT NULL,
    `point` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`player_id`),
    INDEX `idx_ghost_hunt_point` (`point`, `player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `item_option_template` (`id`, `NAME`) VALUES
    (251, 'Xuyên giáp +#%'),
    (252, 'Sát thương Tự sát +#%'),
    (253, 'Sát thương Laze +#%'),
    (254, 'Sát thương Quả cầu Kênh Khi +#%')
ON DUPLICATE KEY UPDATE `NAME` = VALUES(`NAME`);

UPDATE `item_template`
SET `NAME` = 'Sói Địa Ngục',
    `description` = 'Pet 6 cấp, dùng Hồn ma để ép và tăng các chỉ số'
WHERE `id` = 1654;

UPDATE `item_template`
SET `NAME` = 'Hồn ma',
    `description` = 'Dùng ép chỉ số cho Sói Địa Ngục tại Bà Hạt Mít'
WHERE `id` = 1258;

UPDATE `item_template`
SET `NAME` = 'Thịt tươi',
    `description` = 'Dùng nâng cấp Sói Địa Ngục tại Bà Hạt Mít',
    `is_up_to_up` = 1
WHERE `id` = 1549;

UPDATE `item_template`
SET `description` = 'Tăng 5% HP và 5% KI trong 10 phút'
WHERE `id` = 882;

UPDATE `item_template`
SET `description` = 'Laze Namếc xuyên 80% giáp người chơi và Boss trong 10 phút'
WHERE `id` = 888;

COMMIT;
