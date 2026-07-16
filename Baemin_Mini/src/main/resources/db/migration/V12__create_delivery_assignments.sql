CREATE TABLE delivery_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    shipper_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    distance_km DECIMAL(10, 2),
    cancel_reason VARCHAR(255),
    offered_at DATETIME,
    accepted_at DATETIME,
    cancelled_at DATETIME,
    completed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_delivery_assignments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_delivery_assignments_shipper FOREIGN KEY (shipper_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_delivery_assignments_order_status (order_id, status),
    INDEX idx_delivery_assignments_shipper_status (shipper_id, status),
    INDEX idx_delivery_assignments_created_at (created_at)
);