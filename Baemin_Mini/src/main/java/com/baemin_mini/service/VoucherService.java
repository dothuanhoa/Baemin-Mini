package com.baemin_mini.service;

import com.baemin_mini.dto.voucher.VoucherApplyRequest;
import com.baemin_mini.dto.voucher.VoucherApplyResponse;
import com.baemin_mini.dto.voucher.VoucherRequest;
import com.baemin_mini.dto.voucher.VoucherResponse;
import java.util.List;

public interface VoucherService {
    List<VoucherResponse> getValidVouchers();

    List<VoucherResponse> getAllVouchers();

    VoucherResponse getVoucherById(Long id);

    VoucherResponse createVoucher(VoucherRequest request);

    VoucherResponse updateVoucher(Long id, VoucherRequest request);

    void deactivateVoucher(Long id);

    VoucherApplyResponse applyVoucher(VoucherApplyRequest request);
}