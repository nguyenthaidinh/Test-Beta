-- Chạy một lần trên database online trước khi khởi động bản server mới.
-- Item 1532 được đổi từ Rađa kho báu thành Radar dò Vương dùng một lượt.

SET NAMES utf8mb4;
START TRANSACTION;

UPDATE `item_template`
SET `NAME` = 'Radar Dò Vương',
    `description` = 'Dò một map Boss Vương; sau 3 phút sẽ công khai trên kênh thế giới'
WHERE `id` = 1532;

-- Xóa Radar Dò Vương khỏi riêng shop Santa; không xóa item người chơi đang có.
DELETE `item_shop`
FROM `item_shop`
INNER JOIN `tab_shop` ON `tab_shop`.`id` = `item_shop`.`tab_id`
INNER JOIN `shop` ON `shop`.`id` = `tab_shop`.`shop_id`
WHERE `shop`.`tag_name` = 'SANTA'
  AND `item_shop`.`temp_id` = 1532;

-- Bán Radar Dò Vương trong shop sự kiện Chichi: 30.000 Thỏi vàng / 1 cái.
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

DELETE `item_shop`
FROM `item_shop`
INNER JOIN `tab_shop` ON `tab_shop`.`id` = `item_shop`.`tab_id`
WHERE `tab_shop`.`shop_id` = @chichi_shop_id
  AND `item_shop`.`temp_id` = 1532;

SET @royal_boss_radar_shop_item_id := (
    SELECT COALESCE(MAX(`id`), 0) + 1
    FROM `item_shop`
);

INSERT INTO `item_shop`
    (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time`)
SELECT
    @royal_boss_radar_shop_item_id,
    @chichi_tab_id,
    1532,
    1,
    1,
    1,
    30000,
    4028,
    CURRENT_TIMESTAMP
WHERE @chichi_tab_id IS NOT NULL;

COMMIT;
