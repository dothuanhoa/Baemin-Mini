package com.baemin_mini.controller;

import com.baemin_mini.common.ApiResponse;
import com.baemin_mini.dto.VoucherApplyRequest;
import com.baemin_mini.dto.VoucherApplyResponse;
import com.baemin_mini.dto.VoucherResponse;
import com.baemin_mini.service.VoucherService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/valid")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getValidVouchers() {
        return ResponseEntity.ok(ApiResponse.success(voucherService.getValidVouchers()));
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<VoucherApplyResponse>> applyVoucher(@Valid @RequestBody VoucherApplyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.applyVoucher(request)));
    }
}
