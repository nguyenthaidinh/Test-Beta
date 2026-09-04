-- Giai doan 4 su kien Ngu Hanh Son:
-- Tao map moi doc lap cho phong luyen tap. Map 49 chi duoc dung lam mau
-- dia hinh/hinh anh, khong bi sua va khong bi tai su dung.

SET NAMES utf8mb4;

SET @ngu_hanh_son_training_map_id := 189;
SET @ngu_hanh_son_training_waypoints :=
    '[["Ngũ Hành Sơn",0,432,24,456,0,0,122,979,408],["Ngũ Hành Sơn",1176,432,1200,456,0,0,122,979,408]]';

INSERT INTO `map_template` (
    `id`, `NAME`, `zones`, `max_player`, `data`, `type`, `planet_id`,
    `bg_type`, `tile_id`, `bg_id`, `waypoints`, `mobs`, `npcs`, `is_map_double`
)
SELECT
    @ngu_hanh_son_training_map_id,
    'Phòng luyện tập',
    `zones`,
    `max_player`,
    `data`,
    0,
    `planet_id`,
    `bg_type`,
    `tile_id`,
    `bg_id`,
    @ngu_hanh_son_training_waypoints,
    '[]',
    '[]',
    `is_map_double`
FROM `map_template`
WHERE `id` = 49
ON DUPLICATE KEY UPDATE
    `NAME` = VALUES(`NAME`),
    `zones` = VALUES(`zones`),
    `max_player` = VALUES(`max_player`),
    `data` = VALUES(`data`),
    `type` = VALUES(`type`),
    `planet_id` = VALUES(`planet_id`),
    `bg_type` = VALUES(`bg_type`),
    `tile_id` = VALUES(`tile_id`),
    `bg_id` = VALUES(`bg_id`),
    `waypoints` = VALUES(`waypoints`),
    `mobs` = VALUES(`mobs`),
    `npcs` = VALUES(`npcs`),
    `is_map_double` = VALUES(`is_map_double`);
