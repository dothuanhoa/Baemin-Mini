package com.baemin_mini.controller;

import com.baemin_mini.common.ApiResponse;
import com.baemin_mini.dto.auth.CurrentUserResponse;
import com.baemin_mini.dto.user.UserAddressRequest;
import com.baemin_mini.dto.user.UserAddressResponse;
import com.baemin_mini.service.AuthService;
import com.baemin_mini.service.UserAddressService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;
    private final UserAddressService userAddressService;

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(Principal principal) {
        return ApiResponse.success(authService.me(principal.getName()));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/addresses")
    public ApiResponse<List<UserAddressResponse>> getMyAddresses(Principal principal) {
        return ApiResponse.success(userAddressService.getMyAddresses(principal.getName()));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/addresses")
    public ApiResponse<UserAddressResponse> createMyAddress(
            Principal principal,
            @Valid @RequestBody UserAddressRequest request) {
        return ApiResponse.success("Address created", userAddressService.createMyAddress(principal.getName(), request));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/addresses/{id}")
    public ApiResponse<UserAddressResponse> updateMyAddress(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody UserAddressRequest request) {
        return ApiResponse.success("Address updated", userAddressService.updateMyAddress(principal.getName(), id, request));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> deleteMyAddress(Principal principal, @PathVariable Long id) {
        userAddressService.deleteMyAddress(principal.getName(), id);
        return ApiResponse.successMessage("Address deleted");
    }
}