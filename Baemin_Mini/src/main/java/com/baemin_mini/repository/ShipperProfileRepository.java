package com.baemin_mini.repository;

import com.baemin_mini.domain.entity.ShipperProfile;
import com.baemin_mini.domain.enums.ShipperStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipperProfileRepository extends JpaRepository<ShipperProfile, Long> {
    Optional<ShipperProfile> findByUserId(Long userId);

    List<ShipperProfile> findByCurrentStatusAndCurrentLatitudeIsNotNullAndCurrentLongitudeIsNotNullAndLastLocationAtAfter(
            ShipperStatus status,
            LocalDateTime minimumLocationTime);
}
