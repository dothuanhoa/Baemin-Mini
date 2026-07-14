CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    shipper_id BIGINT,
    voucher_id BIGINT,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    delivery_address VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    restaurant_note VARCHAR(255),
    shipper_note VARCHAR(255),
    total_amount DECIMAL(15, 2) NOT NULL,
    discount_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    delivery_fee DECIMAL(15, 2) NOT NULL DEFAULT 0,
    final_amount DECIMAL(15, 2) NOT NULL,
    platform_fee DECIMAL(15, 2) NOT NULL,
    shipper_earning DECIMAL(15, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_orders_customer (customer_id),
    INDEX idx_orders_restaurant (restaurant_id),
    INDEX idx_orders_shipper (shipper_id),
    INDEX idx_orders_status (status)
);

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    item_name_snapshot VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE order_trackings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    note VARCHAR(255),
    actor_role VARCHAR(50) NOT NULL,
    actor_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
