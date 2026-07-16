package com.baemin_mini.dto.shipper;

import com.baemin_mini.domain.enums.ShipperStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShipperCancelOrderRequest(
        @NotBlank(message = "Cancel reason is required")
        @Size(max = 255, message = "Cancel reason must not exceed 255 characters")
        String reason,
        ShipperStatus statusAfterCancel) {
}