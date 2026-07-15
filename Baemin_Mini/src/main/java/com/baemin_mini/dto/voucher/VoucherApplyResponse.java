package com.baemin_mini.dto.voucher;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VoucherApplyResponse {
    private Long voucherId;
    private String code;
    private BigDecimal discountAmount;
    private BigDecimal finalItemsTotal;
}