-- Giai doan 1 su kien Ngu Hanh Son:
-- - Tat ca quai tai map 122, 123 va 124 co 50.000.000 HP.
-- - He so x2 suc manh/tiem nang duoc xu ly trong NPoint.java.
--
-- File nay chi cap nhat cot mobs, khong thay doi loai quai, cap quai,
-- vi tri quai, NPC hay duong noi hien tai cua ba map.

SET NAMES utf8mb4;

SET @ngu_hanh_son_map_122_mobs := '[[50,20,50000000,564,216],[50,20,50000000,348,144],[57,20,50000000,588,408],[50,20,50000000,852,192],[57,20,50000000,324,432],[57,20,50000000,780,432]]';
SET @ngu_hanh_son_map_123_mobs := '[[56,20,50000000,324,384],[56,20,50000000,588,408],[56,20,50000000,780,408],[56,20,50000000,1140,360]]';
SET @ngu_hanh_son_map_124_mobs := '[[57,20,50000000,300,408],[57,20,50000000,492,384],[57,20,50000000,636,384],[57,20,50000000,828,336]]';

UPDATE `map_template`
SET `mobs` = @ngu_hanh_son_map_122_mobs
WHERE `id` = 122;

UPDATE `map_template`
SET `mobs` = @ngu_hanh_son_map_123_mobs
WHERE `id` = 123;

UPDATE `map_template`
SET `mobs` = @ngu_hanh_son_map_124_mobs
WHERE `id` = 124;
