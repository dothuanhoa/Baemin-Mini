CREATE TABLE shipper_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    vehicle_number VARCHAR(50),
    current_status ENUM('AVAILABLE', 'BUSY', 'OFFLINE') NOT NULL DEFAULT 'AVAILABLE',
    current_latitude DECIMAL(10,8),
    current_longitude DECIMAL(11,8),
    last_location_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_shipper_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_shipper_profiles_assignment
    ON shipper_profiles(current_status, last_location_at);
