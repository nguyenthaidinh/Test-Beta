-- ============================================
-- SQL cho NPC Lio Đẹp Trai - Shop Đồ Thần Linh
-- Chạy trên database: team2026
-- ============================================

-- 1. Tạo bảng shop_lio (chạy trước)
CREATE TABLE IF NOT EXISTS shop_lio (
    id INT PRIMARY KEY AUTO_INCREMENT,
    player_id INT NOT NULL,
    seller_name VARCHAR(100) DEFAULT '',
    item_id SMALLINT NOT NULL,
    price INT DEFAULT 100,
    quantity INT DEFAULT 1,
    itemOption TEXT,
    isSold TINYINT DEFAULT 0
);

-- 2. Thêm NPC template (Mabu mập: head=297, body=298, leg=299)
INSERT INTO npc_template (id, name, head, body, leg, avatar) 
VALUES (85, 'Lio Đẹp Trai', 297, 298, 299, 297);

-- 3. Thêm NPC vào Làng Kakarot (Map 14)
-- LƯU Ý: Cần xem cột npcs hiện tại của map 14 rồi thêm NPC vào
-- Chạy lệnh sau để xem:
--   SELECT id, npcs FROM map_template WHERE id = 14;
-- Sau đó thêm [85, 200, 360] vào cuối mảng npcs
-- Ví dụ nếu npcs hiện tại là [[1,100,300],[2,200,300]]
-- thì sửa thành: [[1,100,300],[2,200,300],[85,200,360]]
