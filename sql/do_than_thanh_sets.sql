SET NAMES utf8mb4;

-- Do than thanh uses option 233 with params 101-109 as a server-side marker.
-- Options 191-199 are display-only fallbacks sent to the client.
-- Options 251-254 are reserved for the Hell Wolf, so they must not be deleted.
UPDATE `item_option_template` SET `NAME` = 'Set Gohan' WHERE `id` = 233;
INSERT INTO `item_option_template` (`id`, `NAME`) VALUES
(191, 'Set SVK con\n(5 món +125% sát thương Kamejoko)'),
(192, 'Set Sơn con\n(5 món +50% xuyên giáp, +40% sát thương Kaioken)'),
(193, 'Set Khánh con\n(5 món x2 Thái Dương Hạ San, -50% thời gian Thôi Miên)'),
(194, 'Set Sơn em\n(5 món +150% KI)'),
(195, 'Set Ngao con\n(5 món +130% sát thương Liên Hoàn)'),
(196, 'Set Ngao em\n(5 món +150% sát thương Đẻ Trứng, x2 thời gian hồi)'),
(197, 'Set Bi con\n(5 món +120% HP)'),
(198, 'Set Cầy con\n(5 món +50% HP, x2 phạm vi Tự Sát)'),
(199, 'Set Bình con\n(5 món +60% HP, +30% giáp)'),
(251, 'Xuyên giáp +#%'),
(252, 'Sát thương Tự sát +#%'),
(253, 'Sát thương Laze +#%'),
(254, 'Sát thương Quả cầu Kênh Khi +#%')
ON DUPLICATE KEY UPDATE `NAME` = VALUES(`NAME`);
