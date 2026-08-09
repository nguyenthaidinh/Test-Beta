-- Shop đồ ăn Whis tại Hành tinh Bill.
-- Giá: 10 Thỏi vàng / món. `icon_spec` 4028 là icon_id của item 457 - Thỏi vàng.

START TRANSACTION;

SET @shop_id := (
    SELECT `id`
    FROM `shop`
    WHERE `tag_name` = 'WHIS_FOOD'
    LIMIT 1
);

SET @shop_id := COALESCE(@shop_id, (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `shop`));

INSERT INTO `shop` (`id`, `npc_id`, `tag_name`, `type_shop`)
SELECT @shop_id, 56, 'WHIS_FOOD', 3
WHERE NOT EXISTS (
    SELECT 1
    FROM `shop`
    WHERE `tag_name` = 'WHIS_FOOD'
);

SET @shop_id := (
    SELECT `id`
    FROM `shop`
    WHERE `tag_name` = 'WHIS_FOOD'
    LIMIT 1
);

UPDATE `shop`
SET `npc_id` = 56,
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
SELECT @tab_id, @shop_id, 'Cửa<>hàng'
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
SET `NAME` = 'Cửa<>hàng'
WHERE `id` = @tab_id;

SET @item_shop_id := (
    SELECT `id`
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 880
    LIMIT 1
);
SET @item_shop_id := COALESCE(@item_shop_id, (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `item_shop`));
INSERT INTO `item_shop` (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time`)
SELECT @item_shop_id, @tab_id, 880, 1, 1, 1, 10, 4028, NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 880
);

SET @item_shop_id := (
    SELECT `id`
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 881
    LIMIT 1
);
SET @item_shop_id := COALESCE(@item_shop_id, (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `item_shop`));
INSERT INTO `item_shop` (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time`)
SELECT @item_shop_id, @tab_id, 881, 1, 1, 1, 10, 4028, NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 881
);

SET @item_shop_id := (
    SELECT `id`
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 882
    LIMIT 1
);
SET @item_shop_id := COALESCE(@item_shop_id, (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `item_shop`));
INSERT INTO `item_shop` (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time`)
SELECT @item_shop_id, @tab_id, 882, 1, 1, 1, 10, 4028, NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `item_shop`
    WHERE `tab_id` = @tab_id
      AND `temp_id` = 882
);

UPDATE `item_shop`
SET `is_new` = 1,
    `is_sell` = 1,
    `type_sell` = 1,
    `cost` = 10,
    `icon_spec` = 4028
WHERE `tab_id` = @tab_id
  AND `temp_id` IN (880, 881, 882);

DELETE iso
FROM `item_shop_option` AS iso
INNER JOIN `item_shop` AS ish
    ON ish.`id` = iso.`item_shop_id`
WHERE ish.`tab_id` = @tab_id
  AND ish.`temp_id` IN (880, 881, 882);

COMMIT;
