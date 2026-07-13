package com.baemin_mini.service;

import com.baemin_mini.dto.VoucherApplyRequest;
import com.baemin_mini.dto.VoucherApplyResponse;
import com.baemin_mini.dto.VoucherResponse;
import java.util.List;

public interface VoucherService {
    List<VoucherResponse> getValidVouchers();
    VoucherApplyResponse applyVoucher(VoucherApplyRequest request);
}
