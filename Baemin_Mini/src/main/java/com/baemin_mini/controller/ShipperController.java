package com.baemin_mini.controller;

import com.baemin_mini.common.ApiResponse;
import com.baemin_mini.dto.shipper.ShipperLocationRequest;
import com.baemin_mini.dto.shipper.ShipperProfileResponse;
import com.baemin_mini.service.ShipperProfileService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipper")
@RequiredArgsConstructor
public class ShipperController {
    private final ShipperProfileService shipperProfileService;

    @PreAuthorize("hasRole('SHIPPER')")
    @PutMapping("/location")
    public ApiResponse<ShipperProfileResponse> updateLocation(
            Principal principal,
            @Valid @RequestBody ShipperLocationRequest request) {
        return ApiResponse.success("Shipper location updated", shipperProfileService.updateLocation(principal.getName(), request));
    }
}
