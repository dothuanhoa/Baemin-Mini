-- Baemin Mini Catalog Seed Data
-- Scope: Người 2 - Feature 1: Catalog
-- Phụ thuộc vào V2 đã chạy (có admin, customer01, merchant01, shipper01)

-- Seed Areas
INSERT INTO areas (name) VALUES ('Quận 1, TP.HCM');
INSERT INTO areas (name) VALUES ('Quận 3, TP.HCM');
INSERT INTO areas (name) VALUES ('Quận 10, TP.HCM');

-- Seed Categories
INSERT INTO categories (name, description) VALUES ('Cơm', 'Các loại cơm văn phòng, cơm tấm');
INSERT INTO categories (name, description) VALUES ('Đồ uống', 'Trà sữa, cà phê, nước giải khát');
INSERT INTO categories (name, description) VALUES ('Bún/Phở', 'Các loại bún, phở, hủ tiếu');
INSERT INTO categories (name, description) VALUES ('Ăn vặt', 'Đồ ăn vặt, xiên que, bánh tráng');

-- Lấy ID của merchant01
SET @merchant01_id = (SELECT id FROM users WHERE username = 'merchant01' LIMIT 1);
SET @area_q1_id = (SELECT id FROM areas WHERE name = 'Quận 1, TP.HCM' LIMIT 1);
SET @area_q3_id = (SELECT id FROM areas WHERE name = 'Quận 3, TP.HCM' LIMIT 1);

-- Seed Restaurants cho merchant01
INSERT INTO restaurants (owner_id, area_id, name, address, phone_contact, latitude, longitude, commission_rate, is_open) 
VALUES (@merchant01_id, @area_q1_id, 'Cơm Tấm Sà Bì Chưởng - Quận 1', '123 Trần Hưng Đạo, Quận 1', '0909123456', 10.763428, 106.691763, 10.00, true);

INSERT INTO restaurants (owner_id, area_id, name, address, phone_contact, latitude, longitude, commission_rate, is_open) 
VALUES (@merchant01_id, @area_q3_id, 'Trà Sữa Koi - Quận 3', '456 Võ Văn Tần, Quận 3', '0909654321', 10.772535, 106.685387, 15.00, true);

-- Lấy ID của nhà hàng vừa tạo
SET @comtam_id = (SELECT id FROM restaurants WHERE name = 'Cơm Tấm Sà Bì Chưởng - Quận 1' LIMIT 1);
SET @trasua_id = (SELECT id FROM restaurants WHERE name = 'Trà Sữa Koi - Quận 3' LIMIT 1);

-- Lấy ID Category
SET @cat_com_id = (SELECT id FROM categories WHERE name = 'Cơm' LIMIT 1);
SET @cat_douong_id = (SELECT id FROM categories WHERE name = 'Đồ uống' LIMIT 1);
SET @cat_anvat_id = (SELECT id FROM categories WHERE name = 'Ăn vặt' LIMIT 1);

-- Seed Menu Items cho Cơm Tấm
INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
VALUES (@comtam_id, @cat_com_id, 'Cơm tấm sườn bì chả', 'Sườn cốt lết nướng than hoa, bì heo, chả trứng', 55000.00, null, true);

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
VALUES (@comtam_id, @cat_com_id, 'Cơm tấm ba rọi nướng', 'Ba rọi nướng mỡ hành rưới xốt', 60000.00, null, true);

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
VALUES (@comtam_id, @cat_douong_id, 'Trà đá', 'Trà đá mát lạnh', 5000.00, null, true);

-- Seed Menu Items cho Trà Sữa
INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
VALUES (@trasua_id, @cat_douong_id, 'Trà sữa trân châu hoàng kim', 'Trà sữa đậm vị, trân châu hoàng kim dai giòn', 45000.00, null, true);

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
VALUES (@trasua_id, @cat_anvat_id, 'Bánh tráng trộn', 'Bánh tráng trộn đầy đủ topping', 25000.00, null, true);
