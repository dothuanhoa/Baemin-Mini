package com.baemin_mini.controller;

import com.baemin_mini.common.ApiResponse;
import com.baemin_mini.domain.enums.OrderStatus;
import com.baemin_mini.dto.order.OrderFeeRequest;
import com.baemin_mini.dto.order.OrderFeeResponse;
import com.baemin_mini.dto.order.OrderRequest;
import com.baemin_mini.dto.order.OrderResponse;
import com.baemin_mini.dto.order.OrderTrackingResponse;
import com.baemin_mini.service.OrderService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders/calculate-fee")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderFeeResponse>> calculateFee(@Valid @RequestBody OrderFeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.calculateFee(request)));
    }

    @PostMapping("/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            Principal principal,
            @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.createOrder(principal.getName(), request)));
    }

    @GetMapping("/orders/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getMyOrders(principal.getName())));
    }

    @GetMapping("/orders/{id}/tracking")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT', 'SHIPPER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderTrackingResponse>>> getOrderTracking(
            Principal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderTracking(principal.getName(), id)));
    }

    @GetMapping("/restaurant/orders")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getRestaurantOrders(
            Principal principal,
            @RequestParam(required = false) Long restaurantId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getRestaurantOrders(principal.getName(), restaurantId)));
    }

    @PutMapping("/orders/{id}/status")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            Principal principal,
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateOrderStatus(principal.getName(), id, status)));
    }
}
