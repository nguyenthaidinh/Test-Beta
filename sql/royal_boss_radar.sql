-- Chạy một lần trên database online trước khi khởi động bản server mới.
-- Item 1532 được đổi từ Rađa kho báu thành Radar dò Vương dùng một lượt.

SET NAMES utf8mb4;
START TRANSACTION;

UPDATE `item_template`
SET `NAME` = 'Radar Dò Vương',
    `description` = 'Dò một map Boss Vương; sau 3 phút sẽ công khai trên kênh thế giới'
WHERE `id` = 1532;

COMMIT;
