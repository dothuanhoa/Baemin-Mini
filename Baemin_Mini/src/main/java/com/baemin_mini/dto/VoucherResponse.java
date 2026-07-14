package com.baemin_mini.dto;

import com.baemin_mini.domain.enums.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VoucherResponse {
    private Long id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
