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

-- Thịt tươi (1549) trong shop sự kiện Chichi: 200 Ngọc / 1 cái.
-- Khối này chạy lặp lại an toàn và tự dùng tab 58 nếu database online có tab đó.
SET @chichi_shop_id := (
    SELECT `id`
    FROM `shop`
    WHERE `tag_name` = 'SHOP_CHI_CHI'
    ORDER BY `id`
    LIMIT 1
);

SET @chichi_tab_id := COALESCE(
    (
        SELECT `id`
        FROM `tab_shop`
        WHERE `shop_id` = @chichi_shop_id AND `id` = 58
        LIMIT 1
    ),
    (
        SELECT `id`
        FROM `tab_shop`
        WHERE `shop_id` = @chichi_shop_id
        ORDER BY `id`
        LIMIT 1
    )
);

SET @fresh_meat_shop_item_id := (
    SELECT `item_shop`.`id`
    FROM `item_shop`
    INNER JOIN `tab_shop` ON `tab_shop`.`id` = `item_shop`.`tab_id`
    WHERE `tab_shop`.`shop_id` = @chichi_shop_id
      AND `item_shop`.`temp_id` = 1549
    ORDER BY (`item_shop`.`tab_id` = @chichi_tab_id) DESC, `item_shop`.`id`
    LIMIT 1
);

SET @fresh_meat_shop_item_id := COALESCE(
    @fresh_meat_shop_item_id,
    (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `item_shop`)
);

INSERT INTO `item_shop`
    (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time`)
SELECT
    @fresh_meat_shop_item_id,
    @chichi_tab_id,
    1549,
    1,
    1,
    1,
    200,
    COALESCE((SELECT `icon_id` FROM `item_template` WHERE `id` = 77 LIMIT 1), 932),
    CURRENT_TIMESTAMP
WHERE @chichi_tab_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `item_shop` WHERE `id` = @fresh_meat_shop_item_id
  );

UPDATE `item_shop`
SET `tab_id` = @chichi_tab_id,
    `temp_id` = 1549,
    `is_new` = 1,
    `is_sell` = 1,
    `type_sell` = 1,
    `cost` = 200,
    `icon_spec` = COALESCE(
        (SELECT `icon_id` FROM `item_template` WHERE `id` = 77 LIMIT 1),
        932
    )
WHERE `id` = @fresh_meat_shop_item_id
  AND @chichi_tab_id IS NOT NULL;

DELETE `item_shop`
FROM `item_shop`
INNER JOIN `tab_shop` ON `tab_shop`.`id` = `item_shop`.`tab_id`
WHERE `tab_shop`.`shop_id` = @chichi_shop_id
  AND `item_shop`.`temp_id` = 1549
  AND `item_shop`.`id` <> @fresh_meat_shop_item_id;

DELETE FROM `item_shop_option`
WHERE `item_shop_id` = @fresh_meat_shop_item_id;

COMMIT;
