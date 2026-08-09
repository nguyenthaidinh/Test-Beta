-- Khu 1 cua Dao Hai Tac dung nguyen dia hinh va quai cua map 151.
-- Cong sang khu 2 va NPC vao map se duoc noi sau khi cac khu con lai duoc chot.

INSERT INTO `map_template`
    (`id`, `NAME`, `zones`, `max_player`, `data`, `type`, `planet_id`,
     `bg_type`, `tile_id`, `bg_id`, `waypoints`, `mobs`, `npcs`, `is_map_double`)
SELECT
    186,
    'Đảo Hải Tặc',
    source.`zones`,
    source.`max_player`,
    source.`data`,
    0,
    source.`planet_id`,
    source.`bg_type`,
    source.`tile_id`,
    source.`bg_id`,
    '[]',
    source.`mobs`,
    '[]',
    source.`is_map_double`
FROM `map_template` AS source
WHERE source.`id` = 151
  AND NOT EXISTS (
      SELECT 1
      FROM `map_template` AS existing
      WHERE existing.`id` = 186
  )
LIMIT 1;

-- Sua ban ghi neu migration ban dau da duoc chay truoc khi type map duoc tach khoi Khi Gas.
UPDATE `map_template`
SET `type` = 0,
    `waypoints` = '[]',
    `npcs` = '[]'
WHERE `id` = 186
  AND `NAME` = 'Đảo Hải Tặc';
