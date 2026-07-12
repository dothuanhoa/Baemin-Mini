package com.baemin_mini.service;

import com.baemin_mini.dto.shipper.ShipperLocationRequest;
import com.baemin_mini.dto.shipper.ShipperProfileResponse;

public interface ShipperProfileService {
    ShipperProfileResponse updateLocation(String username, ShipperLocationRequest request);
}
