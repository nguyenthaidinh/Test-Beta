-- Cải trang Cumber (item 1274): HP +40%, KI +40%, SĐ +30%.
-- Khi Xayda mặc cải trang, Tự sát gây x3 sát thương lên người chơi nhưng không tăng lên Boss hoặc quái.
-- Các option và cơ chế thực tế được Java chuẩn hóa; câu SQL này đồng bộ mô tả trên database online.
UPDATE `item_template`
SET `description` = 'Cải trang dành cho Xayda: tăng 40% HP, 40% KI, 30% sức đánh; x3 sát thương Tự sát khi đánh người chơi, không tăng sát thương lên Boss'
WHERE `id` = 1274;
