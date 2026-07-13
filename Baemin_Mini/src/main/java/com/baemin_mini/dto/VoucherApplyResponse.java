package com.baemin_mini.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VoucherApplyResponse {
    private String code;
    private BigDecimal discountAmount;
    private BigDecimal finalItemsTotal; // itemsTotal - discountAmount
}
