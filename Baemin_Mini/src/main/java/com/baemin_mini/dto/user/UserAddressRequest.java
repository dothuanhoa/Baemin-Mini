package com.baemin_mini.dto.user;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UserAddressRequest(
        @Size(max = 50) String title,
        @NotBlank @Size(max = 100) String receiverName,
        @NotBlank @Size(max = 20) String receiverPhone,
        @NotBlank String addressLine,
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal latitude,
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal longitude,
        Boolean isDefault) {
}