-- Giai doan 2 su kien Ngu Hanh Son:
-- Them NPC Duong Tang (template 49) tai Lang Aru, toa do 1100, 432.
-- Code Java se kiem tra nguoi choi da hoan thanh nhiem vu Ma Bu
-- va chuyen sang task 29 truoc khi cho vao map dau tien (map 123).
-- Dam bao ca ba map deu la MAP_NORMAL de nguoi choi, quai va boss
-- trong cung khu co the nhin thay va tuong tac dung nhu map su kien thuong.

SET NAMES utf8mb4;

UPDATE `map_template`
SET `type` = 0
WHERE `id` IN (122, 123, 124);

UPDATE `map_template`
SET `npcs` = CASE
    WHEN TRIM(COALESCE(`npcs`, '')) = '' OR TRIM(`npcs`) = '[]'
        THEN '[[49,1100,432]]'
    ELSE INSERT(`npcs`, CHAR_LENGTH(`npcs`), 0, ',[49,1100,432]')
END
WHERE `id` = 0
  AND COALESCE(`npcs`, '') NOT LIKE '%[49,%';
