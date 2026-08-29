-- Cải trang Bill Bí Ngô (item 739): giống Cải trang Gohan Beast,
-- riêng Sức đánh là 40% thay vì 30%. Các option thực tế được Java chuẩn hóa
-- khi vật phẩm được tải, tạo mới hoặc trang bị.
SET NAMES utf8mb4;

UPDATE `item_template`
SET `description` = 'Tăng 40% sức đánh, 50% HP, KI; +20% sức đánh chí mạng; hút 10% KI; Đẹp +25% sức đánh; tăng 30% sát thương lên Boss. Chỉ có tác dụng khi hợp thể'
WHERE `id` = 739;
