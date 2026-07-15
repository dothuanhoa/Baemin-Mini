package com.baemin_mini.service.impl;

import com.baemin_mini.common.exception.BadRequestException;
import com.baemin_mini.common.exception.ConflictException;
import com.baemin_mini.common.exception.NotFoundException;
import com.baemin_mini.domain.entity.Voucher;
import com.baemin_mini.domain.enums.DiscountType;
import com.baemin_mini.dto.voucher.VoucherApplyRequest;
import com.baemin_mini.dto.voucher.VoucherApplyResponse;
import com.baemin_mini.dto.voucher.VoucherRequest;
import com.baemin_mini.dto.voucher.VoucherResponse;
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
        return voucherRepository
                .findByIsActiveTrueAndIsPublicTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByEndDateAsc(now, now)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getAllVouchers() {
        return voucherRepository.findByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResponse getVoucherById(Long id) {
        return mapToResponse(findVoucher(id));
    }

    @Override
    @Transactional
    public VoucherResponse createVoucher(VoucherRequest request) {
        validateVoucherRequest(request);
        String code = normalizeCode(request.getCode());
        if (voucherRepository.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("Voucher code already exists");
        }

        Voucher voucher = new Voucher();
        applyRequest(voucher, request, code);
        return mapToResponse(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(Long id, VoucherRequest request) {
        validateVoucherRequest(request);
        Voucher voucher = findVoucher(id);
        String code = normalizeCode(request.getCode());
        voucherRepository.findByCodeIgnoreCase(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("Voucher code already exists");
                });

        applyRequest(voucher, request, code);
        return mapToResponse(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public void deactivateVoucher(Long id) {
        Voucher voucher = findVoucher(id);
        voucher.setIsActive(false);
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherApplyResponse applyVoucher(VoucherApplyRequest request) {
        Voucher voucher = voucherRepository.findByCodeIgnoreCaseAndIsActiveTrue(request.getCode())
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
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        return VoucherApplyResponse.builder()
                .voucherId(voucher.getId())
                .code(voucher.getCode())
                .discountAmount(discountAmount)
                .finalItemsTotal(finalTotal)
                .build();
    }

    private Voucher findVoucher(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Voucher not found"));
    }

    private void applyRequest(Voucher voucher, VoucherRequest request, String code) {
        voucher.setCode(code);
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setIsActive(request.getIsActive() == null || request.getIsActive());
        voucher.setIsPublic(request.getIsPublic() == null || request.getIsPublic());
    }

    private void validateVoucherRequest(VoucherRequest request) {
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BadRequestException("Voucher end date must be after start date");
        }
        if (request.getDiscountType() == DiscountType.PERCENT
                && request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("Percent discount must not exceed 100");
        }
        if (request.getDiscountType() == DiscountType.PERCENT
                && request.getMaxDiscount() == null) {
            throw new BadRequestException("Percent voucher should have max discount");
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal itemsTotal) {
        BigDecimal discount = BigDecimal.ZERO;

        if (voucher.getDiscountType() == DiscountType.PERCENT) {
            BigDecimal percentage = voucher.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            discount = itemsTotal.multiply(percentage);

            if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                discount = voucher.getMaxDiscount();
            }
        } else if (voucher.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discount = voucher.getDiscountValue();
            if (discount.compareTo(itemsTotal) > 0) {
                discount = itemsTotal;
            }
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
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
                .isActive(voucher.getIsActive())
                .isPublic(voucher.getIsPublic())
                .build();
    }
}