-- Khu 1 cua Dao Hai Tac dung nguyen dia hinh va quai cua map 151.
-- Whis va Than Huy Diet Beerus se duoc spawn bang BossManager.
-- Quai trong map duoc nang len 50,000,000 HP moi con.
-- Cong sang khu 2 va NPC trong map se duoc noi sau khi cac khu con lai duoc chot.

SET NAMES utf8mb4;

SET @pirate_island_mobs := '[[76,10,50000000,900,360],[73,9,50000000,588,360],[73,9,50000000,1164,360],[74,9,50000000,1068,360],[74,9,50000000,756,360],[75,9,50000000,804,264],[75,9,50000000,972,264],[75,9,50000000,660,264],[75,9,50000000,1116,264]]';
SET @pirate_island_waypoints := '[["Vùng đất lạnh lẽo",1632,264,1656,288,0,0,187,60,408]]';
SET @cold_land_mobs := '[[66,19,50000000,324,288],[67,20,50000000,660,264],[67,20,50000000,1116,216],[66,19,50000000,492,408],[66,19,50000000,804,408],[66,19,50000000,948,408],[66,19,50000000,1284,408]]';
SET @cold_land_waypoints := '[["Đảo Hải Tặc",0,384,24,408,0,0,186,1600,264],["Khu hang động",1656,384,1680,408,0,0,188,1380,1584]]';
SET @cave_mobs := '[[80,21,50000000,132,168],[80,21,50000000,540,168],[80,21,50000000,708,600],[80,21,50000000,1212,648],[80,21,50000000,876,1080],[80,21,50000000,1308,144],[81,21,50000000,348,168],[81,21,50000000,756,168],[81,21,50000000,372,1320],[81,21,50000000,1164,1584],[81,21,50000000,1308,1824],[81,21,50000000,684,1824],[81,21,50000000,492,576],[81,21,50000000,156,888],[80,21,50000000,996,1824],[80,21,50000000,1140,1704],[80,21,50000000,348,576],[81,21,50000000,1068,288]]';
SET @cave_waypoints := '[["Vùng đất lạnh lẽo",1416,1560,1440,1584,0,0,187,1620,384]]';

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
    @pirate_island_waypoints,
    @pirate_island_mobs,
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
SET target.`NAME` = 'Đảo Hải Tặc',
    target.`zones` = source.`zones`,
    target.`max_player` = source.`max_player`,
    target.`data` = source.`data`,
    target.`type` = 0,
    target.`planet_id` = source.`planet_id`,
    target.`bg_type` = source.`bg_type`,
    target.`tile_id` = source.`tile_id`,
    target.`bg_id` = source.`bg_id`,
    target.`waypoints` = @pirate_island_waypoints,
    target.`mobs` = @pirate_island_mobs,
    target.`npcs` = '[]',
    target.`is_map_double` = source.`is_map_double`
WHERE target.`id` = 186;

-- Khu 2: Vung dat lanh leo dung dia hinh map 106, quai 50,000,000 HP.
INSERT INTO `map_template`
    (`id`, `NAME`, `zones`, `max_player`, `data`, `type`, `planet_id`,
     `bg_type`, `tile_id`, `bg_id`, `waypoints`, `mobs`, `npcs`, `is_map_double`)
SELECT
    187,
    'Vùng đất lạnh lẽo',
    source.`zones`,
    source.`max_player`,
    source.`data`,
    0,
    source.`planet_id`,
    source.`bg_type`,
    source.`tile_id`,
    source.`bg_id`,
    @cold_land_waypoints,
    @cold_land_mobs,
    '[]',
    source.`is_map_double`
FROM `map_template` AS source
WHERE source.`id` = 106
  AND NOT EXISTS (
      SELECT 1
      FROM `map_template` AS existing
      WHERE existing.`id` = 187
  )
LIMIT 1;

UPDATE `map_template` AS target
INNER JOIN `map_template` AS source
    ON source.`id` = 106
SET target.`NAME` = 'Vùng đất lạnh lẽo',
    target.`zones` = source.`zones`,
    target.`max_player` = source.`max_player`,
    target.`data` = source.`data`,
    target.`type` = 0,
    target.`planet_id` = source.`planet_id`,
    target.`bg_type` = source.`bg_type`,
    target.`tile_id` = source.`tile_id`,
    target.`bg_id` = source.`bg_id`,
    target.`waypoints` = @cold_land_waypoints,
    target.`mobs` = @cold_land_mobs,
    target.`npcs` = '[]',
    target.`is_map_double` = source.`is_map_double`
WHERE target.`id` = 187;

-- Khu 3: Khu hang dong dung dia hinh map 160, quai 50,000,000 HP.
INSERT INTO `map_template`
    (`id`, `NAME`, `zones`, `max_player`, `data`, `type`, `planet_id`,
     `bg_type`, `tile_id`, `bg_id`, `waypoints`, `mobs`, `npcs`, `is_map_double`)
SELECT
    188,
    'Khu hang động',
    source.`zones`,
    source.`max_player`,
    source.`data`,
    0,
    source.`planet_id`,
    source.`bg_type`,
    source.`tile_id`,
    source.`bg_id`,
    @cave_waypoints,
    @cave_mobs,
    '[]',
    source.`is_map_double`
FROM `map_template` AS source
WHERE source.`id` = 160
  AND NOT EXISTS (
      SELECT 1
      FROM `map_template` AS existing
      WHERE existing.`id` = 188
  )
LIMIT 1;

UPDATE `map_template` AS target
INNER JOIN `map_template` AS source
    ON source.`id` = 160
SET target.`NAME` = 'Khu hang động',
    target.`zones` = source.`zones`,
    target.`max_player` = source.`max_player`,
    target.`data` = source.`data`,
    target.`type` = 0,
    target.`planet_id` = source.`planet_id`,
    target.`bg_type` = source.`bg_type`,
    target.`tile_id` = source.`tile_id`,
    target.`bg_id` = source.`bg_id`,
    target.`waypoints` = @cave_waypoints,
    target.`mobs` = @cave_mobs,
    target.`npcs` = '[]',
    target.`is_map_double` = source.`is_map_double`
WHERE target.`id` = 188;
