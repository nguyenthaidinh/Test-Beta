-- Đồng bộ item 757 và bán trong Shop sự kiện Chi Chi với giá 200 Thỏi vàng.
SET NAMES utf8mb4;
START TRANSACTION;

UPDATE `item_template`
SET `NAME` = 'Tài lộc quá lớn',
    `description` = 'Mở ngẫu nhiên 1-3 phần quà: buff cấp 2, Đá ngũ sắc, Hồn ma, Thỏi vàng, đá nâng cấp hoặc Trang sách cũ; có tỉ lệ cực thấp nhận Phượng hoàng lửa.'
WHERE `id` = 757;

SET @large_fortune_shop_id := (
    SELECT `id`
    FROM `shop`
    WHERE `tag_name` = 'HALLOWEEN_EVENT_SHOP'
    ORDER BY `id`
    LIMIT 1
);

SET @large_fortune_tab_id := (
    SELECT `id`
    FROM `tab_shop`
    WHERE `shop_id` = @large_fortune_shop_id
    ORDER BY `id`
    LIMIT 1
);

SET @large_fortune_item_shop_id := (
    SELECT `id`
    FROM `item_shop`
    WHERE `tab_id` = @large_fortune_tab_id
      AND `temp_id` = 757
    ORDER BY `id`
    LIMIT 1
);

SET @large_fortune_item_shop_id := COALESCE(
    @large_fortune_item_shop_id,
    (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `item_shop`)
);

INSERT INTO `item_shop`
    (`id`, `tab_id`, `temp_id`, `is_new`, `is_sell`, `type_sell`, `cost`, `icon_spec`, `create_time`)
SELECT
    @large_fortune_item_shop_id,
    @large_fortune_tab_id,
    757,
    1,
    1,
    1,
    200,
    4028,
    CURRENT_TIMESTAMP
WHERE @large_fortune_tab_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `item_shop` WHERE `id` = @large_fortune_item_shop_id
  );

UPDATE `item_shop`
SET `tab_id` = @large_fortune_tab_id,
    `temp_id` = 757,
    `is_new` = 1,
    `is_sell` = 1,
    `type_sell` = 1,
    `cost` = 200,
    `icon_spec` = 4028
WHERE `id` = @large_fortune_item_shop_id
  AND @large_fortune_tab_id IS NOT NULL;

DELETE FROM `item_shop_option`
WHERE `item_shop_id` = @large_fortune_item_shop_id;

COMMIT;
