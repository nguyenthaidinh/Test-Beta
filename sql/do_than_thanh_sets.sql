INSERT INTO `item_option_template` (`id`, `NAME`)
VALUES
(251, 'Đồ thần thánh: Set # (1 SVK, 2 Sơn, 3 Khánh, 4 Sơn em, 5 Ngao con, 6 Ngao em)'),
(252, 'Đồ thần thánh: Set Bi con (5 món +120% HP)'),
(253, 'Đồ thần thánh: Set Cầy con (5 món +50% HP, x2 phạm vi Tự Sát)'),
(254, 'Đồ thần thánh: Set Bình con (5 món +60% HP, +30% giáp)')
ON DUPLICATE KEY UPDATE `NAME` = VALUES(`NAME`);
