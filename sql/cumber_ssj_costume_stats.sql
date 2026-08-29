-- Cải trang Cumber SSJ (item 1275): toàn bộ chỉ số giống Bill Bí Ngô,
-- thêm dòng cuối "Đẳng cấp +20% SĐ" cộng chồng sau 40%.
-- Ví dụ: 100% -> 140% -> 168%, tổng hiệu quả SĐ riêng của cải trang là +68%.
SET NAMES utf8mb4;

UPDATE `item_option_template`
SET `NAME` = 'Đẳng cấp +#% SĐ'
WHERE `id` = 39;

UPDATE `item_template`
SET `description` = 'Tăng 40% sức đánh, 50% HP, KI; +20% sức đánh chí mạng; hút 10% KI; Đẹp +25% sức đánh; tăng 30% sát thương lên Boss; Đẳng cấp +20% sức đánh. Chỉ có tác dụng khi hợp thể'
WHERE `id` = 1275;
