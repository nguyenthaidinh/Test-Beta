-- Shop sự kiện Halloween tại NPC ChiChi.
-- Giá dùng Thỏi vàng: Thiệp Halloween 10 TV, Bí ngô 5 TV, Capsule Halloween 100 TV, Hộp Kẹo Ma Quỷ 200 TV, Đá ngũ sắc 1000 TV, Bản đồ truyền thuyết 1000 TV.
-- `icon_spec` 4028 là icon_id của item 457 - Thỏi vàng.

-- May do linh hon: 100 Thoi vang.

START TRANSACTION;

SET @shop_id := (
    SELECT `id`
    FROM `shop`
    WHERE `tag_name` = 'HALLOWEEN_EVENT_SHOP'
    LIMIT 1
);

SET @shop_id := COALESCE(@shop_id, (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `shop`));

INSERT INTO `shop` (`id`, `npc_id`, `tag_name`, `type_shop`)
SELECT @shop_id, 81, 'HALLOWEEN_EVENT_SHOP', 3
WHERE NOT EXISTS (
    SELECT 1
    FROM `shop`
    WHERE `tag_name` = 'HALLOWEEN_EVENT_SHOP'
);

SET @shop_id := (
    SELECT `id`
    FROM `shop`
    WHERE `tag_name` = 'HALLOWEEN_EVENT_SHOP'
    LIMIT 1
);

UPDATE `shop`
SET `npc_id` = 81,
    `type_shop` = 3
WHERE `id` = @shop_id;

SET @tab_id := (
    SELECT `id`
    FROM `tab_shop`
    WHERE `shop_id` = @shop_id
    LIMIT 1
);

SET @tab_id := COALESCE(@tab_id, (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `tab_shop`));

INSERT INTO `tab_shop` (`id`, `shop_id`, `NAME`)
SELECT @tab_id, @shop_id, 'Sự<>kiện'
WHERE NOT EXISTS (
    SELECT 1
    FROM `tab_shop`
    WHERE `shop_id` = @shop_id
);

SET @tab_id := (
    SELECT `id`
    FROM `tab_shop`
    WHERE `shop_id` = @shop_id
    LIMIT 1
);

UPDATE `tab_shop`
SET `NAME` = 'Sự<>kiện'
WHERE `id` = @tab_id;

SET @item_shop_id := (
    SELECT `id`
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 1117
    LIMIT 1
);
SET @item_shop_id := COALESCE(@item_shop_id, (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `item_shop`));
INSERT INTO `item_shop` (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time`)
SELECT @item_shop_id, @tab_id, 1117, 1, 1, 1, 10, 4028, NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 1117
);

SET @item_shop_id := (
    SELECT `id`
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 585
    LIMIT 1
);
SET @item_shop_id := COALESCE(@item_shop_id, (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `item_shop`));
INSERT INTO `item_shop` (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time`)
SELECT @item_shop_id, @tab_id, 585, 1, 1, 1, 5, 4028, NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 585
);

SET @item_shop_id := (
    SELECT `id`
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 674
    LIMIT 1
);
SET @item_shop_id := COALESCE(@item_shop_id, (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `item_shop`));
INSERT INTO `item_shop` (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time`)
SELECT @item_shop_id, @tab_id, 674, 1, 1, 1, 1000, 4028, NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 674
);

SET @item_shop_id := (
    SELECT `id`
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 1264
    LIMIT 1
);
SET @item_shop_id := COALESCE(@item_shop_id, (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `item_shop`));
INSERT INTO `item_shop` (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time`)
SELECT @item_shop_id, @tab_id, 1264, 1, 1, 1, 100, 4028, NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 1264
);

SET @item_shop_id := (
    SELECT `id`
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 1356
    LIMIT 1
);
SET @item_shop_id := COALESCE(@item_shop_id, (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `item_shop`));
INSERT INTO `item_shop` (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time`)
SELECT @item_shop_id, @tab_id, 1356, 1, 1, 1, 200, 4028, NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 1356
);

SET @item_shop_id := (
    SELECT `id`
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 1565
    LIMIT 1
);
SET @item_shop_id := COALESCE(@item_shop_id, (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `item_shop`));
INSERT INTO `item_shop` (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time`)
SELECT @item_shop_id, @tab_id, 1565, 1, 1, 1, 1000, 4028, NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 1565
);

SET @item_shop_id := (
    SELECT `id`
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 818
    LIMIT 1
);
SET @item_shop_id := COALESCE(@item_shop_id, (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `item_shop`));
INSERT INTO `item_shop` (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time`)
SELECT @item_shop_id, @tab_id, 818, 1, 1, 1, 100, 4028, NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 818
);

UPDATE `item_shop`
SET `is_new` = 1,
    `is_sell` = 1,
    `type_sell` = 1,
    `cost` = CASE `temp_id`
        WHEN 1117 THEN 10
        WHEN 585 THEN 5
        WHEN 1264 THEN 100
        WHEN 1356 THEN 200
        WHEN 674 THEN 1000
        WHEN 1565 THEN 1000
        WHEN 818 THEN 100
        ELSE `cost`
    END,
    `icon_spec` = 4028
WHERE `tab_id` = @tab_id
  AND `temp_id` IN (1117, 585, 1264, 1356, 674, 1565, 818);

DELETE iso
FROM `item_shop_option` AS iso
INNER JOIN `item_shop` AS ish
    ON ish.`id` = iso.`item_shop_id`
WHERE ish.`tab_id` = @tab_id
  AND ish.`temp_id` IN (1117, 585, 1264, 1356, 674, 1565, 818);

COMMIT;
