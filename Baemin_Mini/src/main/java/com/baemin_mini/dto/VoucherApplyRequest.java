package com.baemin_mini.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class VoucherApplyRequest {
    @NotBlank(message = "Voucher code is required")
    private String code;

    @NotNull(message = "Items total is required")
    @DecimalMin(value = "0.0", message = "Items total must be positive")
    private BigDecimal itemsTotal;
}
