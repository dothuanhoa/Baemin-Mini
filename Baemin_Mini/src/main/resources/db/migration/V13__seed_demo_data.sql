-- Baemin Mini Demo Seed Data
-- Password for all demo users: 123456
-- Purpose: provide enough realistic data for Swagger/Postman demo.

SET @demo_password_hash = '$2a$10$vMw2oSAObj83WkfJI3HaLuhr6H/w0YmPY9DLZ9GXxTt75A7kxCm7i';

-- Extra demo users
INSERT INTO users (username, password_hash, full_name, phone, email, is_active)
SELECT 'customer02', @demo_password_hash, 'Phạm Minh Anh', '0901000002', 'customer02@baemin.local', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'customer02');

INSERT INTO users (username, password_hash, full_name, phone, email, is_active)
SELECT 'customer03', @demo_password_hash, 'Lê Hoàng Nam', '0901000003', 'customer03@baemin.local', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'customer03');

INSERT INTO users (username, password_hash, full_name, phone, email, is_active)
SELECT 'customer04', @demo_password_hash, 'Trần Ngọc Hân', '0901000004', 'customer04@baemin.local', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'customer04');

INSERT INTO users (username, password_hash, full_name, phone, email, is_active)
SELECT 'merchant02', @demo_password_hash, 'Nguyễn Chủ Quán 2', '0902000002', 'merchant02@baemin.local', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'merchant02');

INSERT INTO users (username, password_hash, full_name, phone, email, is_active)
SELECT 'merchant03', @demo_password_hash, 'Lê Chủ Quán 3', '0902000003', 'merchant03@baemin.local', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'merchant03');

INSERT INTO users (username, password_hash, full_name, phone, email, is_active)
SELECT 'shipper02', @demo_password_hash, 'Nguyễn Tài Xế 2', '0903000002', 'shipper02@baemin.local', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'shipper02');

INSERT INTO users (username, password_hash, full_name, phone, email, is_active)
SELECT 'shipper03', @demo_password_hash, 'Phạm Tài Xế 3', '0903000003', 'shipper03@baemin.local', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'shipper03');

INSERT INTO users (username, password_hash, full_name, phone, email, is_active)
SELECT 'shipper04', @demo_password_hash, 'Trần Tài Xế 4', '0903000004', 'shipper04@baemin.local', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'shipper04');

INSERT INTO users (username, password_hash, full_name, phone, email, is_active)
SELECT 'shipper05', @demo_password_hash, 'Lê Tài Xế 5', '0903000005', 'shipper05@baemin.local', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'shipper05');

-- Role mapping
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'CUSTOMER'
WHERE u.username IN ('customer02', 'customer03', 'customer04');

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'RESTAURANT'
WHERE u.username IN ('merchant02', 'merchant03');

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.name = 'SHIPPER'
WHERE u.username IN ('shipper02', 'shipper03', 'shipper04', 'shipper05');

-- Customer addresses around central Ho Chi Minh City
INSERT INTO user_addresses (user_id, title, receiver_name, receiver_phone, address_line, latitude, longitude, is_default)
SELECT u.id, 'Công ty', 'Phạm Minh Anh', '0901000002', 'Bitexco Financial Tower, Quận 1, TP.HCM', 10.77165200, 106.70414100, TRUE
FROM users u
WHERE u.username = 'customer02'
  AND NOT EXISTS (SELECT 1 FROM user_addresses a WHERE a.user_id = u.id AND a.title = 'Công ty');

INSERT INTO user_addresses (user_id, title, receiver_name, receiver_phone, address_line, latitude, longitude, is_default)
SELECT u.id, 'Nhà riêng', 'Lê Hoàng Nam', '0901000003', 'Hồ Con Rùa, Quận 3, TP.HCM', 10.78293600, 106.69529700, TRUE
FROM users u
WHERE u.username = 'customer03'
  AND NOT EXISTS (SELECT 1 FROM user_addresses a WHERE a.user_id = u.id AND a.title = 'Nhà riêng');

INSERT INTO user_addresses (user_id, title, receiver_name, receiver_phone, address_line, latitude, longitude, is_default)
SELECT u.id, 'Ký túc xá', 'Trần Ngọc Hân', '0901000004', 'Đại học Bách Khoa, Quận 10, TP.HCM', 10.77240300, 106.65795100, TRUE
FROM users u
WHERE u.username = 'customer04'
  AND NOT EXISTS (SELECT 1 FROM user_addresses a WHERE a.user_id = u.id AND a.title = 'Ký túc xá');

-- More areas and categories
INSERT INTO areas (name)
SELECT 'Bình Thạnh, TP.HCM'
WHERE NOT EXISTS (SELECT 1 FROM areas WHERE name = 'Bình Thạnh, TP.HCM');

INSERT INTO areas (name)
SELECT 'Phú Nhuận, TP.HCM'
WHERE NOT EXISTS (SELECT 1 FROM areas WHERE name = 'Phú Nhuận, TP.HCM');

INSERT INTO categories (name, description)
SELECT 'Gà rán', 'Các món gà rán, gà sốt và combo nhanh'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Gà rán');

INSERT INTO categories (name, description)
SELECT 'Pizza', 'Pizza, pasta và món Âu nhanh'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Pizza');

INSERT INTO categories (name, description)
SELECT 'Mì/Bún', 'Các món mì, bún, phở và hủ tiếu'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Mì/Bún');

INSERT INTO categories (name, description)
SELECT 'Đồ chay', 'Món chay, salad và đồ ăn nhẹ lành mạnh'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Đồ chay');

INSERT INTO categories (name, description)
SELECT 'Tráng miệng', 'Bánh ngọt, chè, kem và món tráng miệng'
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Tráng miệng');

-- Demo restaurants
SET @merchant01_id = (SELECT id FROM users WHERE username = 'merchant01' LIMIT 1);
SET @merchant02_id = (SELECT id FROM users WHERE username = 'merchant02' LIMIT 1);
SET @merchant03_id = (SELECT id FROM users WHERE username = 'merchant03' LIMIT 1);
SET @area_q1_id = (SELECT id FROM areas WHERE name = 'Quận 1, TP.HCM' LIMIT 1);
SET @area_q3_id = (SELECT id FROM areas WHERE name = 'Quận 3, TP.HCM' LIMIT 1);
SET @area_q10_id = (SELECT id FROM areas WHERE name = 'Quận 10, TP.HCM' LIMIT 1);
SET @area_bt_id = (SELECT id FROM areas WHERE name = 'Bình Thạnh, TP.HCM' LIMIT 1);
SET @area_pn_id = (SELECT id FROM areas WHERE name = 'Phú Nhuận, TP.HCM' LIMIT 1);

INSERT INTO restaurants (owner_id, area_id, name, address, phone_contact, latitude, longitude, commission_rate, is_open)
SELECT @merchant01_id, @area_q1_id, 'Demo Bún Bò Nguyễn Thái Bình', '58 Nguyễn Thái Bình, Quận 1, TP.HCM', '0911000001', 10.76980000, 106.69990000, 12.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM restaurants WHERE name = 'Demo Bún Bò Nguyễn Thái Bình');

INSERT INTO restaurants (owner_id, area_id, name, address, phone_contact, latitude, longitude, commission_rate, is_open)
SELECT @merchant01_id, @area_q3_id, 'Demo Cơm Gà Xối Mỡ Võ Văn Tần', '210 Võ Văn Tần, Quận 3, TP.HCM', '0911000002', 10.77390000, 106.68980000, 12.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM restaurants WHERE name = 'Demo Cơm Gà Xối Mỡ Võ Văn Tần');

INSERT INTO restaurants (owner_id, area_id, name, address, phone_contact, latitude, longitude, commission_rate, is_open)
SELECT @merchant02_id, @area_q1_id, 'Demo Pizza Pasteur', '120 Pasteur, Quận 1, TP.HCM', '0911000003', 10.77690000, 106.70090000, 15.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM restaurants WHERE name = 'Demo Pizza Pasteur');

INSERT INTO restaurants (owner_id, area_id, name, address, phone_contact, latitude, longitude, commission_rate, is_open)
SELECT @merchant02_id, @area_pn_id, 'Demo Phở Bò Lý Chính Thắng', '86 Lý Chính Thắng, Phú Nhuận, TP.HCM', '0911000004', 10.78650000, 106.68430000, 10.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM restaurants WHERE name = 'Demo Phở Bò Lý Chính Thắng');

INSERT INTO restaurants (owner_id, area_id, name, address, phone_contact, latitude, longitude, commission_rate, is_open)
SELECT @merchant03_id, @area_bt_id, 'Demo Bánh Mì Bà Chiểu', '34 Bạch Đằng, Bình Thạnh, TP.HCM', '0911000005', 10.80200000, 106.69660000, 10.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM restaurants WHERE name = 'Demo Bánh Mì Bà Chiểu');

INSERT INTO restaurants (owner_id, area_id, name, address, phone_contact, latitude, longitude, commission_rate, is_open)
SELECT @merchant03_id, @area_q10_id, 'Demo Cơm Chay Sư Vạn Hạnh', '650 Sư Vạn Hạnh, Quận 10, TP.HCM', '0911000006', 10.77270000, 106.66770000, 8.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM restaurants WHERE name = 'Demo Cơm Chay Sư Vạn Hạnh');

-- Menu items
SET @cat_com_id = (SELECT id FROM categories WHERE name = 'Cơm' LIMIT 1);
SET @cat_douong_id = (SELECT id FROM categories WHERE name = 'Đồ uống' LIMIT 1);
SET @cat_mibun_id = (SELECT id FROM categories WHERE name = 'Mì/Bún' LIMIT 1);
SET @cat_ga_id = (SELECT id FROM categories WHERE name = 'Gà rán' LIMIT 1);
SET @cat_pizza_id = (SELECT id FROM categories WHERE name = 'Pizza' LIMIT 1);
SET @cat_chay_id = (SELECT id FROM categories WHERE name = 'Đồ chay' LIMIT 1);
SET @cat_trangmieng_id = (SELECT id FROM categories WHERE name = 'Tráng miệng' LIMIT 1);

SET @bunbo_id = (SELECT id FROM restaurants WHERE name = 'Demo Bún Bò Nguyễn Thái Bình' LIMIT 1);
SET @comga_id = (SELECT id FROM restaurants WHERE name = 'Demo Cơm Gà Xối Mỡ Võ Văn Tần' LIMIT 1);
SET @pizza_id = (SELECT id FROM restaurants WHERE name = 'Demo Pizza Pasteur' LIMIT 1);
SET @pho_id = (SELECT id FROM restaurants WHERE name = 'Demo Phở Bò Lý Chính Thắng' LIMIT 1);
SET @banhmi_id = (SELECT id FROM restaurants WHERE name = 'Demo Bánh Mì Bà Chiểu' LIMIT 1);
SET @comchay_id = (SELECT id FROM restaurants WHERE name = 'Demo Cơm Chay Sư Vạn Hạnh' LIMIT 1);

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
SELECT @bunbo_id, @cat_mibun_id, 'Bún bò đặc biệt', 'Tô lớn gồm nạm, gân, chả cua và rau sống', 68000.00, '/uploads/demo/bun-bo-dac-biet.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = @bunbo_id AND name = 'Bún bò đặc biệt');

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
SELECT @bunbo_id, @cat_mibun_id, 'Bún bò tái nạm', 'Bún bò vị đậm, nước dùng cay nhẹ', 59000.00, '/uploads/demo/bun-bo-tai-nam.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = @bunbo_id AND name = 'Bún bò tái nạm');

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
SELECT @bunbo_id, @cat_douong_id, 'Sữa đậu nành', 'Sữa đậu nành nhà làm', 12000.00, '/uploads/demo/sua-dau-nanh.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = @bunbo_id AND name = 'Sữa đậu nành');

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
SELECT @comga_id, @cat_com_id, 'Cơm gà xối mỡ đùi lớn', 'Đùi gà giòn, cơm chiên tỏi, đồ chua', 62000.00, '/uploads/demo/com-ga-dui-lon.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = @comga_id AND name = 'Cơm gà xối mỡ đùi lớn');

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
SELECT @comga_id, @cat_com_id, 'Cơm gà sốt mắm tỏi', 'Gà chiên phủ sốt mắm tỏi cay nhẹ', 65000.00, '/uploads/demo/com-ga-mam-toi.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = @comga_id AND name = 'Cơm gà sốt mắm tỏi');

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
SELECT @comga_id, @cat_ga_id, 'Combo cánh gà giòn', '4 cánh gà giòn kèm khoai tây', 72000.00, '/uploads/demo/canh-ga-gion.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = @comga_id AND name = 'Combo cánh gà giòn');

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
SELECT @pizza_id, @cat_pizza_id, 'Pizza hải sản size M', 'Tôm, mực, thanh cua và phô mai mozzarella', 129000.00, '/uploads/demo/pizza-hai-san.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = @pizza_id AND name = 'Pizza hải sản size M');

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
SELECT @pizza_id, @cat_pizza_id, 'Pizza bò bằm size M', 'Bò bằm, hành tây, sốt cà chua Ý', 119000.00, '/uploads/demo/pizza-bo-bam.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = @pizza_id AND name = 'Pizza bò bằm size M');

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
SELECT @pho_id, @cat_mibun_id, 'Phở bò tái chín', 'Phở bò truyền thống với nước dùng trong', 59000.00, '/uploads/demo/pho-bo-tai-chin.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = @pho_id AND name = 'Phở bò tái chín');

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
SELECT @pho_id, @cat_mibun_id, 'Phở bò đặc biệt', 'Tái, nạm, gầu, gân và bò viên', 79000.00, '/uploads/demo/pho-bo-dac-biet.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = @pho_id AND name = 'Phở bò đặc biệt');

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
SELECT @banhmi_id, @cat_com_id, 'Bánh mì thịt nướng', 'Bánh mì giòn, thịt nướng than, đồ chua', 32000.00, '/uploads/demo/banh-mi-thit-nuong.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = @banhmi_id AND name = 'Bánh mì thịt nướng');

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
SELECT @banhmi_id, @cat_trangmieng_id, 'Chè khúc bạch', 'Chè mát lạnh, hạnh nhân và vải', 28000.00, '/uploads/demo/che-khuc-bach.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = @banhmi_id AND name = 'Chè khúc bạch');

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
SELECT @comchay_id, @cat_chay_id, 'Cơm chay thập cẩm', 'Cơm gạo lứt, rau củ, nấm kho và đậu hũ', 52000.00, '/uploads/demo/com-chay-thap-cam.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = @comchay_id AND name = 'Cơm chay thập cẩm');

INSERT INTO menu_items (restaurant_id, category_id, name, description, price, image_url, is_available)
SELECT @comchay_id, @cat_chay_id, 'Bún nấm chay', 'Bún nấm thanh nhẹ, nước dùng rau củ', 49000.00, '/uploads/demo/bun-nam-chay.jpg', TRUE
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE restaurant_id = @comchay_id AND name = 'Bún nấm chay');

-- Demo vouchers
INSERT INTO vouchers (code, discount_type, discount_value, min_order_value, max_discount, start_date, end_date, is_active, is_public)
SELECT 'DEMO10K', 'FIXED_AMOUNT', 10000.00, 50000.00, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'DEMO10K');

INSERT INTO vouchers (code, discount_type, discount_value, min_order_value, max_discount, start_date, end_date, is_active, is_public)
SELECT 'DEMO25', 'PERCENT', 25.00, 100000.00, 40000.00, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'DEMO25');

INSERT INTO vouchers (code, discount_type, discount_value, min_order_value, max_discount, start_date, end_date, is_active, is_public)
SELECT 'SECRET30', 'PERCENT', 30.00, 120000.00, 50000.00, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), TRUE, FALSE
WHERE NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'SECRET30');

INSERT INTO vouchers (code, discount_type, discount_value, min_order_value, max_discount, start_date, end_date, is_active, is_public)
SELECT 'BIGORDER50K', 'FIXED_AMOUNT', 50000.00, 200000.00, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 45 DAY), TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'BIGORDER50K');

-- Shipper profiles near demo restaurants.
SET @shipper01_id = (SELECT id FROM users WHERE username = 'shipper01' LIMIT 1);
SET @shipper02_id = (SELECT id FROM users WHERE username = 'shipper02' LIMIT 1);
SET @shipper03_id = (SELECT id FROM users WHERE username = 'shipper03' LIMIT 1);
SET @shipper04_id = (SELECT id FROM users WHERE username = 'shipper04' LIMIT 1);
SET @shipper05_id = (SELECT id FROM users WHERE username = 'shipper05' LIMIT 1);

INSERT INTO shipper_profiles (user_id, current_status, current_latitude, current_longitude, last_location_at)
SELECT @shipper01_id, 'AVAILABLE', 10.77200000, 106.69830000, NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipper_profiles WHERE user_id = @shipper01_id);

UPDATE shipper_profiles
SET current_status = 'AVAILABLE',
    current_latitude = 10.77200000,
    current_longitude = 106.69830000,
    last_location_at = NOW()
WHERE user_id = @shipper01_id;

INSERT INTO shipper_profiles (user_id, current_status, current_latitude, current_longitude, last_location_at)
SELECT @shipper02_id, 'AVAILABLE', 10.77520000, 106.70100000, NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipper_profiles WHERE user_id = @shipper02_id);

INSERT INTO shipper_profiles (user_id, current_status, current_latitude, current_longitude, last_location_at)
SELECT @shipper03_id, 'AVAILABLE', 10.78900000, 106.69200000, NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipper_profiles WHERE user_id = @shipper03_id);

INSERT INTO shipper_profiles (user_id, current_status, current_latitude, current_longitude, last_location_at)
SELECT @shipper04_id, 'OFFLINE', 10.80600000, 106.71100000, NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipper_profiles WHERE user_id = @shipper04_id);

INSERT INTO shipper_profiles (user_id, current_status, current_latitude, current_longitude, last_location_at)
SELECT @shipper05_id, 'BUSY', 10.78100000, 106.69400000, NOW()
WHERE NOT EXISTS (SELECT 1 FROM shipper_profiles WHERE user_id = @shipper05_id);

-- Demo orders for restaurant, customer, shipper and admin screens.
SET @customer01_id = (SELECT id FROM users WHERE username = 'customer01' LIMIT 1);
SET @customer02_id = (SELECT id FROM users WHERE username = 'customer02' LIMIT 1);
SET @customer03_id = (SELECT id FROM users WHERE username = 'customer03' LIMIT 1);
SET @customer04_id = (SELECT id FROM users WHERE username = 'customer04' LIMIT 1);
SET @admin_id = (SELECT id FROM users WHERE username = 'admin' LIMIT 1);
SET @voucher_demo10k_id = (SELECT id FROM vouchers WHERE code = 'DEMO10K' LIMIT 1);
SET @voucher_demo25_id = (SELECT id FROM vouchers WHERE code = 'DEMO25' LIMIT 1);

SET @bunbo_special_id = (SELECT id FROM menu_items WHERE restaurant_id = @bunbo_id AND name = 'Bún bò đặc biệt' LIMIT 1);
SET @bunbo_suadau_id = (SELECT id FROM menu_items WHERE restaurant_id = @bunbo_id AND name = 'Sữa đậu nành' LIMIT 1);
SET @comga_dui_id = (SELECT id FROM menu_items WHERE restaurant_id = @comga_id AND name = 'Cơm gà xối mỡ đùi lớn' LIMIT 1);
SET @pizza_hs_id = (SELECT id FROM menu_items WHERE restaurant_id = @pizza_id AND name = 'Pizza hải sản size M' LIMIT 1);
SET @pho_dacbiet_id = (SELECT id FROM menu_items WHERE restaurant_id = @pho_id AND name = 'Phở bò đặc biệt' LIMIT 1);
SET @comchay_id_item = (SELECT id FROM menu_items WHERE restaurant_id = @comchay_id AND name = 'Cơm chay thập cẩm' LIMIT 1);

INSERT INTO orders (
    customer_id, restaurant_id, shipper_id, voucher_id,
    receiver_name, receiver_phone, delivery_address, latitude, longitude,
    restaurant_note, shipper_note,
    total_amount, discount_amount, delivery_fee, final_amount,
    platform_fee, shipper_earning,
    payment_method, payment_status, status, created_at
)
SELECT
    @customer02_id, @bunbo_id, NULL, @voucher_demo10k_id,
    'Phạm Minh Anh', '0901000002', 'Bitexco Financial Tower, Quận 1, TP.HCM', 10.77165200, 106.70414100,
    'Ít cay giúp em', 'Gọi trước khi đến',
    148000.00, 10000.00, 16000.00, 154000.00,
    16560.00, 16000.00,
    'COD', 'UNPAID', 'PLACED', DATE_SUB(NOW(), INTERVAL 20 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE receiver_phone = '0901000002' AND delivery_address = 'Bitexco Financial Tower, Quận 1, TP.HCM' AND status = 'PLACED');

SET @order_placed_id = (
    SELECT id FROM orders
    WHERE receiver_phone = '0901000002'
      AND delivery_address = 'Bitexco Financial Tower, Quận 1, TP.HCM'
      AND status = 'PLACED'
    ORDER BY id DESC LIMIT 1
);

INSERT INTO order_items (order_id, menu_item_id, item_name_snapshot, quantity, unit_price)
SELECT @order_placed_id, @bunbo_special_id, 'Bún bò đặc biệt', 2, 68000.00
WHERE NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @order_placed_id AND menu_item_id = @bunbo_special_id);

INSERT INTO order_items (order_id, menu_item_id, item_name_snapshot, quantity, unit_price)
SELECT @order_placed_id, @bunbo_suadau_id, 'Sữa đậu nành', 1, 12000.00
WHERE NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @order_placed_id AND menu_item_id = @bunbo_suadau_id);

INSERT INTO order_trackings (order_id, status, note, actor_role, actor_id, created_at)
SELECT @order_placed_id, 'PLACED', 'Demo customer placed order', 'CUSTOMER', @customer02_id, DATE_SUB(NOW(), INTERVAL 20 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM order_trackings WHERE order_id = @order_placed_id AND status = 'PLACED');

INSERT INTO orders (
    customer_id, restaurant_id, shipper_id, voucher_id,
    receiver_name, receiver_phone, delivery_address, latitude, longitude,
    restaurant_note, shipper_note,
    total_amount, discount_amount, delivery_fee, final_amount,
    platform_fee, shipper_earning,
    payment_method, payment_status, status, created_at
)
SELECT
    @customer03_id, @pizza_id, NULL, @voucher_demo25_id,
    'Lê Hoàng Nam', '0901000003', 'Hồ Con Rùa, Quận 3, TP.HCM', 10.78293600, 106.69529700,
    'Cắt pizza sẵn giúp mình', 'Giao ở cổng chính',
    258000.00, 40000.00, 18000.00, 236000.00,
    32700.00, 18000.00,
    'COD', 'UNPAID', 'PREPARING', DATE_SUB(NOW(), INTERVAL 12 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE receiver_phone = '0901000003' AND delivery_address = 'Hồ Con Rùa, Quận 3, TP.HCM' AND status = 'PREPARING');

SET @order_preparing_id = (
    SELECT id FROM orders
    WHERE receiver_phone = '0901000003'
      AND delivery_address = 'Hồ Con Rùa, Quận 3, TP.HCM'
      AND status = 'PREPARING'
    ORDER BY id DESC LIMIT 1
);

INSERT INTO order_items (order_id, menu_item_id, item_name_snapshot, quantity, unit_price)
SELECT @order_preparing_id, @pizza_hs_id, 'Pizza hải sản size M', 2, 129000.00
WHERE NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @order_preparing_id AND menu_item_id = @pizza_hs_id);

INSERT INTO order_trackings (order_id, status, note, actor_role, actor_id, created_at)
SELECT @order_preparing_id, 'PLACED', 'Demo customer placed order', 'CUSTOMER', @customer03_id, DATE_SUB(NOW(), INTERVAL 12 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM order_trackings WHERE order_id = @order_preparing_id AND status = 'PLACED');

INSERT INTO order_trackings (order_id, status, note, actor_role, actor_id, created_at)
SELECT @order_preparing_id, 'PREPARING', 'Demo restaurant accepted and started preparing', 'RESTAURANT', @merchant02_id, DATE_SUB(NOW(), INTERVAL 8 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM order_trackings WHERE order_id = @order_preparing_id AND status = 'PREPARING');

INSERT INTO orders (
    customer_id, restaurant_id, shipper_id, voucher_id,
    receiver_name, receiver_phone, delivery_address, latitude, longitude,
    restaurant_note, shipper_note,
    total_amount, discount_amount, delivery_fee, final_amount,
    platform_fee, shipper_earning,
    payment_method, payment_status, status, created_at
)
SELECT
    @customer04_id, @pho_id, @shipper05_id, NULL,
    'Trần Ngọc Hân', '0901000004', 'Đại học Bách Khoa, Quận 10, TP.HCM', 10.77240300, 106.65795100,
    'Cho nhiều hành', 'Giao tại cổng Lý Thường Kiệt',
    79000.00, 0.00, 22000.00, 101000.00,
    7900.00, 22000.00,
    'COD', 'UNPAID', 'DELIVERING', DATE_SUB(NOW(), INTERVAL 35 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE receiver_phone = '0901000004' AND delivery_address = 'Đại học Bách Khoa, Quận 10, TP.HCM' AND status = 'DELIVERING');

SET @order_delivering_id = (
    SELECT id FROM orders
    WHERE receiver_phone = '0901000004'
      AND delivery_address = 'Đại học Bách Khoa, Quận 10, TP.HCM'
      AND status = 'DELIVERING'
    ORDER BY id DESC LIMIT 1
);

INSERT INTO order_items (order_id, menu_item_id, item_name_snapshot, quantity, unit_price)
SELECT @order_delivering_id, @pho_dacbiet_id, 'Phở bò đặc biệt', 1, 79000.00
WHERE NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @order_delivering_id AND menu_item_id = @pho_dacbiet_id);

INSERT INTO delivery_assignments (order_id, shipper_id, status, distance_km, accepted_at, created_at)
SELECT @order_delivering_id, @shipper05_id, 'ACCEPTED', 1.70, DATE_SUB(NOW(), INTERVAL 25 MINUTE), DATE_SUB(NOW(), INTERVAL 25 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM delivery_assignments WHERE order_id = @order_delivering_id AND shipper_id = @shipper05_id AND status = 'ACCEPTED');

INSERT INTO order_trackings (order_id, status, note, actor_role, actor_id, created_at)
SELECT @order_delivering_id, 'PLACED', 'Demo customer placed order', 'CUSTOMER', @customer04_id, DATE_SUB(NOW(), INTERVAL 35 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM order_trackings WHERE order_id = @order_delivering_id AND status = 'PLACED');

INSERT INTO order_trackings (order_id, status, note, actor_role, actor_id, created_at)
SELECT @order_delivering_id, 'PREPARING', 'Demo restaurant prepared order', 'RESTAURANT', @merchant02_id, DATE_SUB(NOW(), INTERVAL 28 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM order_trackings WHERE order_id = @order_delivering_id AND status = 'PREPARING');

INSERT INTO order_trackings (order_id, status, note, actor_role, actor_id, created_at)
SELECT @order_delivering_id, 'DELIVERING', 'Demo shipper started delivery', 'SHIPPER', @shipper05_id, DATE_SUB(NOW(), INTERVAL 20 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM order_trackings WHERE order_id = @order_delivering_id AND status = 'DELIVERING');

INSERT INTO orders (
    customer_id, restaurant_id, shipper_id, voucher_id,
    receiver_name, receiver_phone, delivery_address, latitude, longitude,
    restaurant_note, shipper_note,
    total_amount, discount_amount, delivery_fee, final_amount,
    platform_fee, shipper_earning,
    payment_method, payment_status, status, created_at
)
SELECT
    @customer01_id, @comchay_id, @shipper03_id, NULL,
    'Nguyễn Văn Khách', '0901000001', '123 Nguyễn Trãi, Quận 1, TP.HCM', 10.77210900, 106.69827800,
    'Không lấy ớt', 'Gọi điện nếu không thấy khách',
    104000.00, 0.00, 20000.00, 124000.00,
    8320.00, 20000.00,
    'COD', 'PAID', 'DELIVERED', DATE_SUB(NOW(), INTERVAL 2 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE receiver_phone = '0901000001' AND delivery_address = '123 Nguyễn Trãi, Quận 1, TP.HCM' AND status = 'DELIVERED');

SET @order_delivered_id = (
    SELECT id FROM orders
    WHERE receiver_phone = '0901000001'
      AND delivery_address = '123 Nguyễn Trãi, Quận 1, TP.HCM'
      AND status = 'DELIVERED'
    ORDER BY id DESC LIMIT 1
);

INSERT INTO order_items (order_id, menu_item_id, item_name_snapshot, quantity, unit_price)
SELECT @order_delivered_id, @comchay_id_item, 'Cơm chay thập cẩm', 2, 52000.00
WHERE NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @order_delivered_id AND menu_item_id = @comchay_id_item);

INSERT INTO delivery_assignments (order_id, shipper_id, status, distance_km, accepted_at, completed_at, created_at)
SELECT @order_delivered_id, @shipper03_id, 'COMPLETED', 2.80, DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 60 MINUTE), DATE_SUB(NOW(), INTERVAL 90 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM delivery_assignments WHERE order_id = @order_delivered_id AND shipper_id = @shipper03_id AND status = 'COMPLETED');

INSERT INTO order_trackings (order_id, status, note, actor_role, actor_id, created_at)
SELECT @order_delivered_id, 'PLACED', 'Demo customer placed order', 'CUSTOMER', @customer01_id, DATE_SUB(NOW(), INTERVAL 2 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM order_trackings WHERE order_id = @order_delivered_id AND status = 'PLACED');

INSERT INTO order_trackings (order_id, status, note, actor_role, actor_id, created_at)
SELECT @order_delivered_id, 'PREPARING', 'Demo restaurant prepared order', 'RESTAURANT', @merchant03_id, DATE_SUB(NOW(), INTERVAL 110 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM order_trackings WHERE order_id = @order_delivered_id AND status = 'PREPARING');

INSERT INTO order_trackings (order_id, status, note, actor_role, actor_id, created_at)
SELECT @order_delivered_id, 'DELIVERING', 'Demo shipper started delivery', 'SHIPPER', @shipper03_id, DATE_SUB(NOW(), INTERVAL 90 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM order_trackings WHERE order_id = @order_delivered_id AND status = 'DELIVERING');

INSERT INTO order_trackings (order_id, status, note, actor_role, actor_id, created_at)
SELECT @order_delivered_id, 'DELIVERED', 'Demo shipper completed delivery', 'SHIPPER', @shipper03_id, DATE_SUB(NOW(), INTERVAL 60 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM order_trackings WHERE order_id = @order_delivered_id AND status = 'DELIVERED');

INSERT INTO orders (
    customer_id, restaurant_id, shipper_id, voucher_id,
    receiver_name, receiver_phone, delivery_address, latitude, longitude,
    restaurant_note, shipper_note,
    total_amount, discount_amount, delivery_fee, final_amount,
    platform_fee, shipper_earning,
    payment_method, payment_status, status, created_at
)
SELECT
    @customer02_id, @comga_id, NULL, NULL,
    'Phạm Minh Anh', '0901000002', 'Landmark 81, Bình Thạnh, TP.HCM', 10.79466200, 106.72181400,
    'Demo đơn đã hủy', NULL,
    62000.00, 0.00, 21000.00, 83000.00,
    7440.00, 21000.00,
    'COD', 'CANCELLED', 'CANCELLED', DATE_SUB(NOW(), INTERVAL 3 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE receiver_phone = '0901000002' AND delivery_address = 'Landmark 81, Bình Thạnh, TP.HCM' AND status = 'CANCELLED');

SET @order_cancelled_id = (
    SELECT id FROM orders
    WHERE receiver_phone = '0901000002'
      AND delivery_address = 'Landmark 81, Bình Thạnh, TP.HCM'
      AND status = 'CANCELLED'
    ORDER BY id DESC LIMIT 1
);

INSERT INTO order_items (order_id, menu_item_id, item_name_snapshot, quantity, unit_price)
SELECT @order_cancelled_id, @comga_dui_id, 'Cơm gà xối mỡ đùi lớn', 1, 62000.00
WHERE NOT EXISTS (SELECT 1 FROM order_items WHERE order_id = @order_cancelled_id AND menu_item_id = @comga_dui_id);

INSERT INTO order_trackings (order_id, status, note, actor_role, actor_id, created_at)
SELECT @order_cancelled_id, 'PLACED', 'Demo customer placed order', 'CUSTOMER', @customer02_id, DATE_SUB(NOW(), INTERVAL 3 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM order_trackings WHERE order_id = @order_cancelled_id AND status = 'PLACED');

INSERT INTO order_trackings (order_id, status, note, actor_role, actor_id, created_at)
SELECT @order_cancelled_id, 'CANCELLED', 'Demo order cancelled by admin', 'ADMIN', @admin_id, DATE_SUB(NOW(), INTERVAL 170 MINUTE)
WHERE NOT EXISTS (SELECT 1 FROM order_trackings WHERE order_id = @order_cancelled_id AND status = 'CANCELLED');
