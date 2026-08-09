-- Khu 1 cua Dao Hai Tac dung nguyen dia hinh va quai cua map 151.
-- Whis va Than Huy Diet Beerus se duoc spawn bang BossManager.
-- Cong sang khu 2 va NPC trong map se duoc noi sau khi cac khu con lai duoc chot.

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
-- Dong thoi ep lai quai/dia hinh tu map 151 neu DB da co map 186 tu truoc.
UPDATE `map_template` AS target
INNER JOIN `map_template` AS source
    ON source.`id` = 151
SET target.`zones` = source.`zones`,
    target.`max_player` = source.`max_player`,
    target.`data` = source.`data`,
    target.`type` = 0,
    target.`planet_id` = source.`planet_id`,
    target.`bg_type` = source.`bg_type`,
    target.`tile_id` = source.`tile_id`,
    target.`bg_id` = source.`bg_id`,
    target.`waypoints` = '[]',
    target.`mobs` = source.`mobs`,
    target.`npcs` = '[]',
    target.`is_map_double` = source.`is_map_double`
WHERE target.`id` = 186
  AND target.`NAME` = 'Đảo Hải Tặc';
