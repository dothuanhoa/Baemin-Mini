package com.baemin_mini.service.impl;

import com.baemin_mini.common.exception.BadRequestException;
import com.baemin_mini.common.exception.NotFoundException;
import com.baemin_mini.domain.entity.Voucher;
import com.baemin_mini.domain.enums.DiscountType;
import com.baemin_mini.dto.VoucherApplyRequest;
import com.baemin_mini.dto.VoucherApplyResponse;
import com.baemin_mini.dto.VoucherResponse;
import com.baemin_mini.repository.VoucherRepository;
import com.baemin_mini.service.VoucherService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getValidVouchers() {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> validVouchers = voucherRepository.findValidVouchers(now);
        return validVouchers.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherApplyResponse applyVoucher(VoucherApplyRequest request) {
        Voucher voucher = voucherRepository.findByCodeAndIsActiveTrue(request.getCode())
                .orElseThrow(() -> new NotFoundException("Voucher not found or inactive"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            throw new BadRequestException("Voucher is expired or not yet active");
        }

        BigDecimal itemsTotal = request.getItemsTotal();
        if (itemsTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new BadRequestException("Order total does not meet the minimum requirement for this voucher");
        }

        BigDecimal discountAmount = calculateDiscount(voucher, itemsTotal);
        BigDecimal finalTotal = itemsTotal.subtract(discountAmount);
        // Ngăn chặn trường hợp lỗi số âm
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        return VoucherApplyResponse.builder()
                .code(voucher.getCode())
                .discountAmount(discountAmount)
                .finalItemsTotal(finalTotal)
                .build();
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal itemsTotal) {
        BigDecimal discount = BigDecimal.ZERO;

        if (voucher.getDiscountType() == DiscountType.PERCENT) {
            // items_total * discount_value / 100
            BigDecimal percentage = voucher.getDiscountValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            discount = itemsTotal.multiply(percentage);

            // min(discount, max_discount)
            if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                discount = voucher.getMaxDiscount();
            }
        } else if (voucher.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            // min(discount_value, items_total)
            discount = voucher.getDiscountValue();
            if (discount.compareTo(itemsTotal) > 0) {
                discount = itemsTotal;
            }
        }

        return discount;
    }

    private VoucherResponse mapToResponse(Voucher voucher) {
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .minOrderValue(voucher.getMinOrderValue())
                .maxDiscount(voucher.getMaxDiscount())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .build();
    }
}
