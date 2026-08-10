SET NAMES utf8mb4;

-- Do than thanh uses option 233 with params 101-109 as a server-side marker.
-- Options 251-254 are display-only fallbacks sent to the client.
UPDATE `item_option_template` SET `NAME` = 'Set Gohan' WHERE `id` = 233;
INSERT INTO `item_option_template` (`id`, `NAME`) VALUES
(251, 'Set Thần Thánh #'),
(252, 'Set Bi con (5 món +120% HP)'),
(253, 'Set Cầy con (5 món +50% HP, x2 phạm vi Tự Sát)'),
(254, 'Set Bình con (5 món +60% HP, +30% giáp)')
ON DUPLICATE KEY UPDATE `NAME` = VALUES(`NAME`);
