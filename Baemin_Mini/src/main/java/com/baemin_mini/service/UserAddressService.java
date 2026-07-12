package com.baemin_mini.service;

import com.baemin_mini.dto.user.UserAddressRequest;
import com.baemin_mini.dto.user.UserAddressResponse;
import java.util.List;

public interface UserAddressService {
    List<UserAddressResponse> getMyAddresses(String username);

    UserAddressResponse createMyAddress(String username, UserAddressRequest request);

    UserAddressResponse updateMyAddress(String username, Long id, UserAddressRequest request);

    void deleteMyAddress(String username, Long id);
}