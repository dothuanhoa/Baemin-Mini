package com.baemin_mini.controller;

import com.baemin_mini.common.ApiResponse;
import com.baemin_mini.dto.OrderFeeRequest;
import com.baemin_mini.dto.OrderFeeResponse;
import com.baemin_mini.dto.OrderRequest;
import com.baemin_mini.dto.OrderResponse;
import com.baemin_mini.dto.OrderTrackingResponse;
import com.baemin_mini.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<OrderFeeResponse>> calculateFee(@Valid @RequestBody OrderFeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.calculateFee(request)));
    }

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        // Tạm thời hardcode customerId = 1L do chưa có module lấy ID từ Token của Customer
        Long customerId = 1L;
        return ResponseEntity.ok(ApiResponse.success(orderService.createOrder(customerId, request)));
    }

    @GetMapping("/orders/my-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders() {
        Long customerId = 1L;
        return ResponseEntity.ok(ApiResponse.success(orderService.getMyOrders(customerId)));
    }

    @GetMapping("/orders/{id}/tracking")
    public ResponseEntity<ApiResponse<List<OrderTrackingResponse>>> getOrderTracking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderTracking(id)));
    }

    @GetMapping("/restaurant/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getRestaurantOrders(@RequestParam Long restaurantId) {
        // Tạm thời truyền parameter thay vì lấy từ Token của Owner
        return ResponseEntity.ok(ApiResponse.success(orderService.getRestaurantOrders(restaurantId)));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        // Tạm thời hardcode actor
        return ResponseEntity.ok(ApiResponse.success(orderService.updateOrderStatus(id, status, 1L, "RESTAURANT_OWNER")));
    }
}
