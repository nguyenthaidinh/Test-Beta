-- NPC Cristiano Bi tại Làng Kakarot (map 14).
-- Hoạt ảnh lấy từ cải trang VIP: head 569, body 472, leg 473.
SET NAMES utf8mb4;

START TRANSACTION;

INSERT INTO `npc_template` (`id`, `NAME`, `head`, `body`, `leg`, `avatar`)
VALUES (86, 'Cristiano Bi', 569, 472, 473, 5304)
ON DUPLICATE KEY UPDATE
    `NAME` = VALUES(`NAME`),
    `head` = VALUES(`head`),
    `body` = VALUES(`body`),
    `leg` = VALUES(`leg`),
    `avatar` = VALUES(`avatar`);

UPDATE `map_template`
SET `npcs` = REPLACE(
    REPLACE(`npcs`, '[86,80,408]', '[86,1080,408]'),
    '[86,120,408]', '[86,1080,408]'
)
WHERE `id` = 14;

UPDATE `map_template`
SET `npcs` = CASE
    WHEN TRIM(`npcs`) = '[]' THEN '[[86,1080,408]]'
    ELSE CONCAT(LEFT(TRIM(`npcs`), CHAR_LENGTH(TRIM(`npcs`)) - 1), ',[86,1080,408]]')
END
WHERE `id` = 14
  AND REPLACE(`npcs`, ' ', '') NOT LIKE '%[86,%';

COMMIT;
