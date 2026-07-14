package com.baemin_mini.service.impl;

import com.baemin_mini.common.exception.NotFoundException;
import com.baemin_mini.domain.entity.ShipperProfile;
import com.baemin_mini.domain.entity.User;
import com.baemin_mini.domain.enums.ShipperStatus;
import com.baemin_mini.dto.shipper.ShipperLocationRequest;
import com.baemin_mini.dto.shipper.ShipperProfileResponse;
import com.baemin_mini.repository.ShipperProfileRepository;
import com.baemin_mini.repository.UserRepository;
import com.baemin_mini.service.ShipperProfileService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShipperProfileServiceImpl implements ShipperProfileService {
    private final UserRepository userRepository;
    private final ShipperProfileRepository shipperProfileRepository;

    @Override
    @Transactional
    public ShipperProfileResponse updateLocation(String username, ShipperLocationRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ShipperProfile profile = shipperProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> createProfile(user));

        profile.setCurrentLatitude(request.latitude());
        profile.setCurrentLongitude(request.longitude());
        profile.setLastLocationAt(LocalDateTime.now());
        return toResponse(shipperProfileRepository.save(profile));
    }

    private ShipperProfile createProfile(User user) {
        ShipperProfile profile = new ShipperProfile();
        profile.setUser(user);
        profile.setCurrentStatus(ShipperStatus.AVAILABLE);
        return profile;
    }

    private ShipperProfileResponse toResponse(ShipperProfile profile) {
        return new ShipperProfileResponse(
                profile.getId(),
                profile.getVehicleNumber(),
                profile.getCurrentStatus(),
                profile.getCurrentLatitude(),
                profile.getCurrentLongitude(),
                profile.getLastLocationAt());
    }
}
