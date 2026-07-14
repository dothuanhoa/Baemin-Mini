package com.baemin_mini.service.impl;

import com.baemin_mini.common.exception.NotFoundException;
import com.baemin_mini.domain.entity.User;
import com.baemin_mini.domain.entity.UserAddress;
import com.baemin_mini.dto.user.UserAddressRequest;
import com.baemin_mini.dto.user.UserAddressResponse;
import com.baemin_mini.repository.UserAddressRepository;
import com.baemin_mini.repository.UserRepository;
import com.baemin_mini.service.UserAddressService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserAddressResponse> getMyAddresses(String username) {
        User user = findUser(username);
        return userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserAddressResponse createMyAddress(String username, UserAddressRequest request) {
        User user = findUser(username);
        if (Boolean.TRUE.equals(request.isDefault())) {
            clearDefaultAddresses(user.getId());
        }

        UserAddress address = new UserAddress();
        address.setUser(user);
        applyRequest(address, request);
        UserAddress saved = userAddressRepository.save(address);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserAddressResponse updateMyAddress(String username, Long id, UserAddressRequest request) {
        User user = findUser(username);
        UserAddress address = userAddressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Address not found"));
        if (Boolean.TRUE.equals(request.isDefault())) {
            clearDefaultAddresses(user.getId());
        }
        applyRequest(address, request);
        return toResponse(userAddressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteMyAddress(String username, Long id) {
        User user = findUser(username);
        UserAddress address = userAddressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Address not found"));
        userAddressRepository.delete(address);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void clearDefaultAddresses(Long userId) {
        List<UserAddress> addresses = userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        for (UserAddress address : addresses) {
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                address.setIsDefault(false);
            }
        }
        userAddressRepository.saveAll(addresses);
    }

    private void applyRequest(UserAddress address, UserAddressRequest request) {
        address.setTitle(blankToNull(request.title()));
        address.setReceiverName(request.receiverName().trim());
        address.setReceiverPhone(request.receiverPhone().trim());
        address.setAddressLine(request.addressLine().trim());
        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());
        address.setIsDefault(Boolean.TRUE.equals(request.isDefault()));
    }

    private UserAddressResponse toResponse(UserAddress address) {
        return new UserAddressResponse(
                address.getId(),
                address.getTitle(),
                address.getReceiverName(),
                address.getReceiverPhone(),
                address.getAddressLine(),
                address.getLatitude(),
                address.getLongitude(),
                address.getIsDefault(),
                address.getCreatedAt(),
                address.getUpdatedAt());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}