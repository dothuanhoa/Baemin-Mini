-- Baemin Mini Core Seed Data
-- Password for all demo users: 123456

INSERT INTO roles (name) VALUES
('CUSTOMER'),
('RESTAURANT'),
('SHIPPER'),
('ADMIN');

INSERT INTO users (username, password_hash, full_name, phone, email, is_active) VALUES
('admin', '$2a$10$vMw2oSAObj83WkfJI3HaLuhr6H/w0YmPY9DLZ9GXxTt75A7kxCm7i', 'Quản trị hệ thống', '0909000001', 'admin@baemin.local', TRUE),
('customer01', '$2a$10$vMw2oSAObj83WkfJI3HaLuhr6H/w0YmPY9DLZ9GXxTt75A7kxCm7i', 'Nguyễn Văn Khách', '0901000001', 'customer01@baemin.local', TRUE),
('merchant01', '$2a$10$vMw2oSAObj83WkfJI3HaLuhr6H/w0YmPY9DLZ9GXxTt75A7kxCm7i', 'Trần Chủ Quán', '0902000001', 'merchant01@baemin.local', TRUE),
('shipper01', '$2a$10$vMw2oSAObj83WkfJI3HaLuhr6H/w0YmPY9DLZ9GXxTt75A7kxCm7i', 'Lê Tài Xế', '0903000001', 'shipper01@baemin.local', TRUE);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.username = 'admin';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'CUSTOMER'
WHERE u.username = 'customer01';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'RESTAURANT'
WHERE u.username = 'merchant01';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'SHIPPER'
WHERE u.username = 'shipper01';

INSERT INTO user_addresses (
    user_id,
    title,
    receiver_name,
    receiver_phone,
    address_line,
    latitude,
    longitude,
    is_default
)
SELECT
    u.id,
    'Nhà riêng',
    'Nguyễn Văn Khách',
    '0901000001',
    '123 Nguyễn Trãi, Quận 1, TP.HCM',
    10.77210900,
    106.69827800,
    TRUE
FROM users u
WHERE u.username = 'customer01';