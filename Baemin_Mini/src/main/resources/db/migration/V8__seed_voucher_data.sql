-- Seed data cho bảng vouchers
-- Mã giảm 20% (Giảm tối đa 30,000, Đơn tối thiểu 100,000)
INSERT INTO vouchers (code, discount_type, discount_value, min_order_value, max_discount, start_date, end_date, is_active)
VALUES ('WELCOME20', 'PERCENT', 20.00, 100000.00, 30000.00, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), TRUE);

-- Mã giảm thẳng 15,000 (Đơn tối thiểu 50,000)
INSERT INTO vouchers (code, discount_type, discount_value, min_order_value, max_discount, start_date, end_date, is_active)
VALUES ('FREESHIP15K', 'FIXED_AMOUNT', 15000.00, 50000.00, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), TRUE);

-- Mã giảm 50% (Giảm tối đa 50,000, Đơn tối thiểu 150,000)
INSERT INTO vouchers (code, discount_type, discount_value, min_order_value, max_discount, start_date, end_date, is_active)
VALUES ('SIEUDEAL50', 'PERCENT', 50.00, 150000.00, 50000.00, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), TRUE);

-- Mã đã hết hạn (Để test case hết hạn)
INSERT INTO vouchers (code, discount_type, discount_value, min_order_value, max_discount, start_date, end_date, is_active)
VALUES ('EXPIRED10K', 'FIXED_AMOUNT', 10000.00, 0.00, NULL, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), TRUE);

-- Mã bị khóa (Để test case in-active)
INSERT INTO vouchers (code, discount_type, discount_value, min_order_value, max_discount, start_date, end_date, is_active)
VALUES ('LOCKED10K', 'FIXED_AMOUNT', 10000.00, 0.00, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), FALSE);
