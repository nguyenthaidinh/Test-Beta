-- Cải trang Cumber (item 1274): HP +40%, KI +40%, SĐ +30%.
-- Khi Xayda mặc cải trang, Tự sát gây x3 sát thương lên người chơi nhưng không tăng lên Boss hoặc quái.
-- Bán tại tab sự kiện shop Chi Chi với giá 5.000.000 Thỏi vàng.

SET NAMES utf8mb4;
START TRANSACTION;

UPDATE `item_template`
SET `description` = 'Cải trang dành cho Xayda: tăng 40% HP, 40% KI, 30% sức đánh; x3 sát thương Tự sát khi đánh người chơi, không tăng sát thương lên Boss'
WHERE `id` = 1274;

SET @cumber_chichi_shop_id := (
    SELECT `id`
    FROM `shop`
    WHERE `tag_name` = 'SHOP_CHI_CHI'
    ORDER BY `id`
    LIMIT 1
);

SET @cumber_chichi_tab_id := COALESCE(
    (
        SELECT `id`
        FROM `tab_shop`
        WHERE `shop_id` = @cumber_chichi_shop_id AND `id` = 58
        LIMIT 1
    ),
    (
        SELECT `id`
        FROM `tab_shop`
        WHERE `shop_id` = @cumber_chichi_shop_id
        ORDER BY `id`
        LIMIT 1
    )
);

SET @cumber_shop_item_id := (
    SELECT `item_shop`.`id`
    FROM `item_shop`
    INNER JOIN `tab_shop` ON `tab_shop`.`id` = `item_shop`.`tab_id`
    WHERE `tab_shop`.`shop_id` = @cumber_chichi_shop_id
      AND `item_shop`.`temp_id` = 1274
    ORDER BY (`item_shop`.`tab_id` = @cumber_chichi_tab_id) DESC, `item_shop`.`id`
    LIMIT 1
);

SET @cumber_shop_item_id := COALESCE(
    @cumber_shop_item_id,
    (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `item_shop`)
);

INSERT INTO `item_shop`
    (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time`)
SELECT
    @cumber_shop_item_id,
    @cumber_chichi_tab_id,
    1274,
    1,
    1,
    1,
    5000000,
    4028,
    CURRENT_TIMESTAMP
WHERE @cumber_chichi_tab_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `item_shop` WHERE `id` = @cumber_shop_item_id
  );

UPDATE `item_shop`
SET `tab_id` = @cumber_chichi_tab_id,
    `temp_id` = 1274,
    `is_new` = 1,
    `is_sell` = 1,
    `type_sell` = 1,
    `cost` = 5000000,
    `icon_spec` = 4028
WHERE `id` = @cumber_shop_item_id
  AND @cumber_chichi_tab_id IS NOT NULL;

DELETE `item_shop`
FROM `item_shop`
INNER JOIN `tab_shop` ON `tab_shop`.`id` = `item_shop`.`tab_id`
WHERE `tab_shop`.`shop_id` = @cumber_chichi_shop_id
  AND `item_shop`.`temp_id` = 1274
  AND `item_shop`.`id` <> @cumber_shop_item_id;

DELETE FROM `item_shop_option`
WHERE `item_shop_id` = @cumber_shop_item_id;

INSERT INTO `item_shop_option` (`item_shop_id`, `option_id`, `param`)
SELECT @cumber_shop_item_id, 77, 40
FROM `item_shop`
WHERE `id` = @cumber_shop_item_id
UNION ALL
SELECT @cumber_shop_item_id, 103, 40
FROM `item_shop`
WHERE `id` = @cumber_shop_item_id
UNION ALL
SELECT @cumber_shop_item_id, 50, 30
FROM `item_shop`
WHERE `id` = @cumber_shop_item_id;

COMMIT;
